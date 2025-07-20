package br.com.lagom.solarinverterbot.spreadsheet;

import br.com.lagom.solarinverterbot.dto.PortalPlantCredentialImportDTO;
import br.com.lagom.solarinverterbot.model.Client;
import br.com.lagom.solarinverterbot.model.InverterManufacturer;
import br.com.lagom.solarinverterbot.model.Plant;
import br.com.lagom.solarinverterbot.model.PlantCredential;
import br.com.lagom.solarinverterbot.repository.ClientRepository;
import br.com.lagom.solarinverterbot.repository.InverterManufacturerRepository;
import br.com.lagom.solarinverterbot.repository.PlantCredentialRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PortalPlantCredentialImportService {

    @Autowired
    private InverterManufacturerRepository inverterManufacturerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlantCredentialRepository plantCredentialRepository;

    @Transactional
    public PortalPlantCredentialImportDTO importClientsCredentials(File fileExcel) {
        List<Client> clientList = new ArrayList<>();
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
                rowIterator.next(); // pula uma linha, a primeira já é o cabeçalho
                int countRow = 2;

                while(rowIterator.hasNext()){
                    Row row = rowIterator.next();
                    try{
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
                                p.getCredential().getUsername().equals(username) &&
                                        p.getInverterManufacturer().getId().equals(manufacturer.getId())
                        );

                        if(!plantAlreadyExists){
                            Plant plant = new Plant();
                            plant.setName(username);
                            plant.setClient(client);
                            plant.setCredential(credential);
                            plant.setInverterManufacturer(manufacturer);

                            client.getPlants().add(plant);
                            credential.getPlants().add(plant);
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Erro na linha %d: %s".formatted(countRow, e.getMessage()));
                    }
                    countRow++;
                }
            }
            workbook.close();
        } catch (RuntimeException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clientRepository.saveAll(clientList);
        return new PortalPlantCredentialImportDTO(clientList.size(), invalidClient);
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
