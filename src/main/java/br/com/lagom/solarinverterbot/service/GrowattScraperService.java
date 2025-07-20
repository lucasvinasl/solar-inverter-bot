package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.model.Client;
import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.repository.InverterManufacturerRepository;
import br.com.lagom.solarinverterbot.scraper.GrowattElementMap;
import br.com.lagom.solarinverterbot.scraper.PortalScraper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class GrowattScraperService implements PortalScraper {

    private static final String BASE_DOWNLOAD_PATH = System.getProperty("user.home") + File.separator + "Documents"
            + File.separator + "SpreadsheetAutomation" + File.separator + "growatt_spreadsheets";
    private static final ZoneId LOCAL_ZONE_ID = ZoneId.of("America/Fortaleza");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
    private final List<Client> unprocessedClients = new CopyOnWriteArrayList<>();
    private static final int MAX_ATTEMPTS = 2;

    @Autowired
    private EnergyYearlyDataRepository energyYearlyDataRepository;

    @Autowired
    private ProcessingQueueEntryRepository processingQueueEntryRepository;

    @Autowired
    private static InverterManufacturerRepository inverterManufacturerRepository;

    private static InverterManufacturer url = inverterManufacturerRepository.findByNameIgnoreCase("GROWATT")
            .orElseThrow(() -> new EntityNotFoundException("GROWATT NÃO CADASTRADO."));

    private static final String URL_GROWATT = url.getPortalUrl();

    @Override
    public boolean isPortalAvailable(String manufacturerName) {
        Optional<InverterManufacturer> manufacturer = inverterManufacturerRepository.findByNameIgnoreCase(manufacturerName);
        return manufacturer.isPresent();
    }

    @Override
    public void webScrapingService(Client client) {

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                doWebScraping(client);
                return;
            } catch (Exception ex) {
                log.error("Tentativa {}/{} falhou para cliente {}: {}",
                        attempt, MAX_ATTEMPTS, client.getName(), ex.getMessage(), ex);

                if (attempt == MAX_ATTEMPTS) {
                    unprocessedClients.add(client);
                    log.warn("Cliente {} marcado como NÃO PROCESSADO.", client.getName());
                } else {
                    log.info("Nova tentativa para o cliente {}", client.getName());
                }
            }
        }
    }


    private void doWebScraping(Client client) throws Exception {

        String downloadDir = BASE_DOWNLOAD_PATH + File.separator + clientNameClean(client.getName());
        createDirectoryIfNotExists(downloadDir);

        ChromeOptions options = buildChromeOptions(downloadDir);

        WebDriver driver = new ChromeDriver(options);
        try{

            driver.get(URL_GROWATT);
            driver.manage().window().maximize();

            GrowattElementMap map = new GrowattElementMap();
            login(client, driver, map);

            try {
                plantsLits(driver, map);
            } catch (RuntimeException e) {
                log.info("Cliente sem múltiplas plantas: {}", e.getMessage());
            }

            clickOnEnergy(driver, map);
            Thread.sleep(2000);
            clickOnEnergyMonth(driver, map);
            Thread.sleep(1000);
            clickOnExport(driver, map);
            Thread.sleep(1000);
            clickOnAnnualReport(driver, map);
            Thread.sleep(1000);

            File file = renameDownloadedFileSimple(downloadDir, Duration.ofSeconds(60));
            if (file != null) {
                ProcessingQueueEntry entry = new ProcessingQueueEntry(
                        client, file.getAbsolutePath(), file.getName());
                processingQueueEntryRepository.save(entry);
                log.info("Planilha '{}' enfileirada (cliente {}, fila {}).",
                        entry.getFileName(), client.getName(), entry.getId());
            } else {
                throw new IllegalStateException("Arquivo não baixado/renomeado.");
            }
        } catch (Exception e) {
            log.error("Erro durante scraping: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private ChromeOptions buildChromeOptions(String downloadDir) {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safeBrowse.enabled", true);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private String clientNameClean(String name){
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private void createDirectoryIfNotExists(String directoryPath){
        Path path = Paths.get(directoryPath);
        try{
            if(!Files.exists(path)){
                Files.createDirectories(path);
                log.info("Pasta Criada: {} ", directoryPath);
            }else{
                log.info("Pasta já Existe: {} ", directoryPath);
            }

        }catch (IOException e){
            log.error("Erro ao criar o diretório de download {}: {}", directoryPath, e.getMessage());
            throw new RuntimeException("Não foi possível preparar o diretório de download.", e);
        }
    }

    private File renameDownloadedFileSimple(String downloadDirectory, Duration timeout) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        File downloadedFile = null;

        while (System.currentTimeMillis() < endTime) {
            File dir = new File(downloadDirectory);
            File[] files = dir.listFiles((d, name) -> !name.endsWith(".crdownload") && !name.endsWith(".tmp"));

            if (files != null && files.length > 0) {
                downloadedFile = Arrays.stream(files)
                        .max(Comparator.comparingLong(File::lastModified))
                        .orElse(null);

                if (downloadedFile != null) {
                    String originalFullName = downloadedFile.getName();
                    String fileExtension = "";
                    String fileNameWithoutExtension = originalFullName;

                    int dotIndex = originalFullName.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < originalFullName.length() - 1) {
                        fileExtension = originalFullName.substring(dotIndex);
                        fileNameWithoutExtension = originalFullName.substring(0, dotIndex);
                    }

                    String timestamp = ZonedDateTime.now(LOCAL_ZONE_ID).format(DATE_TIME_FORMATTER);

                    String newFileName = String.format("%s_%s%s",
                            fileNameWithoutExtension,
                            timestamp,
                            fileExtension);

                    Path newFilePath = Paths.get(downloadDirectory, newFileName);

                    try {
                        Files.move(downloadedFile.toPath(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("Arquivo baixado renomeado de '{}' para '{}'", originalFullName, newFileName);
                        return newFilePath.toFile();
                    } catch (IOException e) {
                        log.error("Erro ao renomear o arquivo baixado '{}': {}", originalFullName, e.getMessage());
                        return null;
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupção durante a espera pelo download do arquivo.");
                break;
            }
        }
        log.error("Nenhum arquivo baixado encontrado no diretório '{}' após {} segundos.", downloadDirectory, timeout.getSeconds());
        return null;
    }


    private void login(Client client, WebDriver driver, GrowattElementMap webElementMapped) throws InterruptedException {
        webElementMapped.waitAndMapLoginElements(driver, Duration.ofSeconds(10));

        webElementMapped.usernameInput.sendKeys(client.getUsername());
        webElementMapped.passwordInput.sendKeys(client.getPassword());

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElementMapped.loginButton);
        Thread.sleep(2000);
        webElementMapped.loginButton.click();
        Thread.sleep(2000);

        log.info("Login efetuado com sucesso! \n Cliente: {} - Portal: {}", client.getName(), client.getInverterManufacturer().getName());
    }

    // verificar se tem mais de uma planta ou mais de um inversor no caso de Ethan
    public List<WebElement> plantsLits(WebDriver driver, GrowattElementMap webElementMapped){

        webElementMapped.waitAndMapPlantListVerify(driver, Duration.ofSeconds(10));

        //List<WebElement> plants = driver.findElements(By.xpath("//div[@id='selectPlant-con']/ul/li"));
        List<WebElement> plants = webElementMapped.selectPlant.findElements(By.xpath(".//li"));
        //List<WebElement> plants = webElementMapped.selectPlant.findElements(By.tagName("li"));
        if(plants.size() > 1){
            for(WebElement plant : plants){
                log.info("Planta encontrada: {}", plant.getText());
            }
        }else if(plants.size() == 1){
            log.warn("Apenas uma planta encontrada ou nenhuma.");
        }else{
            return null;
        }

        return plants;
    }

    private void clickOnEnergy(WebDriver driver, GrowattElementMap webElementMapped){
        webElementMapped.waitAndMapEnergyButton(driver, Duration.ofSeconds(10));
        webElementMapped.energyButton.click();
    }

    private void clickOnEnergyMonth(WebDriver driver, GrowattElementMap webElementMapped){
        webElementMapped.waitAndMapEnergyButtonMonth(driver, Duration.ofSeconds(10));
        webElementMapped.energyButtonMonth.click();
    }

    private void clickOnExport(WebDriver driver, GrowattElementMap webElementMapped){
        webElementMapped.waitAndMapExportButton(driver, Duration.ofSeconds(10));
        webElementMapped.exportButton.click();
    }

    private void clickOnAnnualReport(WebDriver driver, GrowattElementMap webElementMapped){
        webElementMapped.waitAndMapAnnualReport(driver, Duration.ofSeconds(10));
        webElementMapped.annualReportOption.click();
    }

}
