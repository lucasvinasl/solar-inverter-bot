package br.com.lagom.solarinverterbot.service;

import br.com.lagom.solarinverterbot.dto.ImportCredentialsQueueResponseDTO;
import br.com.lagom.solarinverterbot.enums.StatusQueueEnum;
import br.com.lagom.solarinverterbot.model.*;
import br.com.lagom.solarinverterbot.repository.*;
import br.com.lagom.solarinverterbot.model.ImportCredentialsQueueEntry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ImportCredentialsService {

    public static final int IMPORT_QUEUE_TIMEOUT_MINUTES = 30;

    @Autowired
    private InverterManufacturerRepository inverterManufacturerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlantCredentialRepository plantCredentialRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ImportCredentialsQueueEntryRepository importCredentialsQueueEntryRepository;

    @Transactional
    public void addImportCredentialsQueue(String filePath, Company company){
//        File file = new File("C:/Users/usuario/Documents/SpreadsheetAutomation/Client_Credentials_By_Manufacturer.xlsx");
        File file = new File(filePath);
        Company currentCompany = companyRepository.findById(company.getId())
                .orElseThrow(()->new EntityNotFoundException("Company não encontrada."));

        ImportCredentialsQueueEntry importCredentialsQueueEntry = new ImportCredentialsQueueEntry();
        importCredentialsQueueEntry.setCompany(currentCompany);
        importCredentialsQueueEntry.setFilePath(file.getAbsolutePath());
        importCredentialsQueueEntry.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        importCredentialsQueueEntry.setStatusCredentials(StatusQueueEnum.PENDING);
        importCredentialsQueueEntryRepository.save(importCredentialsQueueEntry);
    }

    @Transactional
    public void importSheetCredentials() {
        ZonedDateTime timoutLimit = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(IMPORT_QUEUE_TIMEOUT_MINUTES);
        List<ImportCredentialsQueueEntry> importQueueList = importCredentialsQueueEntryRepository.findTop1Pending(timoutLimit);

        if(importQueueList.isEmpty()){
            return;
        }

        ImportCredentialsQueueEntry importCredentialEntry = importQueueList.getFirst();
        importCredentialEntry.setStatusCredentials(StatusQueueEnum.IN_PROGRESS);
        importCredentialsQueueEntryRepository.saveAndFlush(importCredentialEntry);

        try{
            importCredentials(importCredentialEntry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    protected void importCredentials(ImportCredentialsQueueEntry importCredentialEntry){

        File fileExcel = new File(importCredentialEntry.getFilePath());
        List<Client> clientList = new ArrayList<>();
        Company currentcompany = companyRepository.findById(importCredentialEntry.getCompany().getId())
                .orElseThrow(() -> new EntityNotFoundException("Company não encontrada."));
        int invalidClient = 0;
        Workbook workbook = null;

        try(FileInputStream fileInputStream = new FileInputStream(fileExcel)){
            if(fileExcel.getName().toLowerCase().endsWith(".xlsx")){
                workbook = new XSSFWorkbook(fileInputStream);
            }else if(fileExcel.getName().toLowerCase().endsWith(".xls")){
                workbook = new HSSFWorkbook(fileInputStream);
            }else{
                throw new IllegalArgumentException("Formato do excel inválido.");
            }


            for(int i = 0; i < workbook.getNumberOfSheets(); i++){
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                InverterManufacturer manufacturer = inverterManufacturerRepository.findByNameIgnoreCase(sheetName)
                        .orElseThrow(()-> new EntityNotFoundException(String.format("Fabritante %s não cadastrado.", sheetName)));

                Row headers = sheet.getRow(0);
                if (headers == null ||
                        !"Cliente".equalsIgnoreCase(headers.getCell(0).getStringCellValue().trim()) ||
                        !"Usuário".equalsIgnoreCase(headers.getCell(1).getStringCellValue().trim()) ||
                        !"Senha".equalsIgnoreCase(headers.getCell(2).getStringCellValue().trim())) {
                    throw new IllegalArgumentException("Cabeçalhos fora do padrão esperado: Cliente | Usuário | Senha");
                }

                Iterator<Row> rowIterator = sheet.iterator();
                rowIterator.next();
                int countRow = 2;

                while(rowIterator.hasNext()){
                    Row row = rowIterator.next();
                    try{
                        if (row == null) {
                            invalidClient++;
                            countRow++;
                            continue;
                        }
                        String clientName = verifyCellToString(row.getCell(0));
                        String username = verifyCellToString(row.getCell(1));
                        String password = verifyCellToString(row.getCell(2));

                        if(clientName.isBlank() || username.isBlank() || password.isBlank()){
                            invalidClient++;
                            countRow++;
                            continue;
                        }

                        Client client = clientRepository.findByNameIgnoreCase(clientName)
                                .orElseGet(() -> {
                                    Client newClient = new Client();
                                    newClient.setName(clientName);
                                    newClient.setCompany(currentcompany);
                                    return clientRepository.save(newClient);
                                });

                        if (!clientList.contains(client)) {
                            clientList.add(client);
                        }

                        PlantCredential credential = plantCredentialRepository.findByUsername(username)
                                .orElseGet(() -> {
                                    PlantCredential pc = new PlantCredential();
                                    pc.setUsername(username);
                                    pc.setPassword(password);
                                    pc.setClient(client);
                                    pc.setManufacturer(manufacturer);
                                    return plantCredentialRepository.save(pc);
                                });

                        boolean plantAlreadyExists = client.getPlants().stream().anyMatch(p ->
                                p.getCredential() != null && p.getCredential().getUsername().equals(username) &&
                                        p.getInverterManufacturer().getId().equals(manufacturer.getId())
                        );

                        if(!plantAlreadyExists){
                            Plant plant = new Plant();
                            plant.setName(username);
                            plant.setClient(client);
                            plant.setCredential(credential);
                            plant.setInverterManufacturer(manufacturer);

                            if (client.getPlants() != null) {
                                client.getPlants().add(plant);
                            }
                            if (credential.getPlants() != null) {
                                credential.getPlants().add(plant);
                            }
                        }
                    } catch (Exception e) {
                        String errorMessage = e.getMessage() != null ? e.getMessage() : "NullPointerException - verifique se há valores nulos na linha";
                        throw new RuntimeException("Erro na linha %d: %s".formatted(countRow, errorMessage));
                    }
                    countRow++;
                }
            }
            workbook.close();
        } catch (RuntimeException | FileNotFoundException e) {
            importCredentialEntry.setStatusCredentials(StatusQueueEnum.FAILED);
            importCredentialsQueueEntryRepository.save(importCredentialEntry);
            e.printStackTrace();
        } catch (IOException e) {
            importCredentialEntry.setStatusCredentials(StatusQueueEnum.FAILED);
            importCredentialsQueueEntryRepository.save(importCredentialEntry);
            throw new RuntimeException(e);
        }
        clientRepository.saveAll(clientList);
        importCredentialEntry.setStatusCredentials(StatusQueueEnum.COMPLETED);
        importCredentialEntry.setSaved(clientList.size());
        importCredentialEntry.setInvalid(invalidClient);
        importCredentialEntry.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        importCredentialsQueueEntryRepository.saveAndFlush(importCredentialEntry);
    }

    public Page<ImportCredentialsQueueResponseDTO> getAllImportCredentialsQueue(Long companyId,int page, int size){
        PageRequest pageable = PageRequest.of(page,size);
        Company company = companyRepository.findById(companyId).orElseThrow(()-> new EntityNotFoundException("Company não encontrada."));
        Page<ImportCredentialsQueueEntry> queue = importCredentialsQueueEntryRepository.findAllByCompanyIdOrderByCreatedAtDesc(company.getId(), pageable);
        return queue.map(entry -> new ImportCredentialsQueueResponseDTO(
                entry.getId(),
                entry.getSaved(),
                entry.getInvalid(),
                entry.getStatusCredentials())
        );
    }

    private String verifyCellToString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> throw new RuntimeException("Tipo de célula não suportado: " + cell.getCellType());
        };
    }
}
