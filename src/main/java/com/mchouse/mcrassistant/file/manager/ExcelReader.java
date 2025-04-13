package com.mchouse.mcrassistant.file.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import com.mchouse.mcrassistant.model.ControleCondominio;
import com.mchouse.mcrassistant.model.ExclColumn;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelReader {

    private static final String ERRO_CELL = "#ERRO_CELL";
    private final String[] columnNames = {"IMÓVEL LOCADO", "LOCATÁRIO", "LOCADOR", "IMOVEL", "Adm", "Email", "ENVIO", "RETORNO"};
    private LinkedList<ExclColumn> validColumnsList;

    public LinkedList<ControleCondominio> readFile(String filePath) {
        if(filePath==null){
            throw new RuntimeException("Nenhum arquivo encontrado no caminho especificado");
        }

        LinkedList<ControleCondominio> controlesCondominioList = new LinkedList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if(getHeaderRow(sheet)==null){
                getLogger().error("Excel file doesn't have the right columns.");
                throw new RuntimeException("Excel file doesn't have the right columns.");
            }

            identifyHeaderPositions(sheet);
            removerCabecalho(sheet);
             String columnNames[] = {"IMÓVEL LOCADO", "LOCATÁRIO", "LOCADOR", "IMOVEL", "Adm", "Email", "ENVIO", "RETORNO"};
            for (Row row : sheet) {
                if (row == null) continue;
                ControleCondominio cc = new ControleCondominio();

                cc.setImovelLocado(getCellValueByName(columnNames[0], row));
                cc.setLocatario(getCellValueByName(columnNames[1], row));
                cc.setLocador(getCellValueByName(columnNames[2], row));
                cc.setImovel(getCellValueByName(columnNames[3], row));
                cc.setAdm(getCellValueByName(columnNames[4], row));
                cc.setEmail(getCellValueByName(columnNames[5], row));
                cc.setEnviado("OK".equalsIgnoreCase(getCellValueByName(columnNames[6], row)));
                cc.setRetorno("OK".equalsIgnoreCase(getCellValueByName(columnNames[7], row)));

                cc.setValidado(cc.getImovelLocado().equalsIgnoreCase(cc.getImovel()) && !cc.getImovelLocado().equals(ERRO_CELL));

                if (hasEmail(cc)) {
                    controlesCondominioList.add(cc);
                }
            }
        } catch (IOException e) {
            getLogger().error(e);
            throw new RuntimeException(e);
        }

        return controlesCondominioList;
    }

    private String getCellValueByName(String name, Row row){
        for(ExclColumn ec :validColumnsList){
            if(ec.getName().equals(name)){
                return getCellValueAsString(row.getCell(ec.getId()));
            }
        }
        return "";
    }

    private String getCellValueAsString(Cell cell){
        if(cell !=null)
        return switch (cell.getCellType()) {
            case _NONE -> String.valueOf(cell.getRichStringCellValue());
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue();
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case ERROR -> String.valueOf(cell.getErrorCellValue());
            default -> String.valueOf(cell.getRichStringCellValue());
        };
        else return ERRO_CELL;
    }

    private void identifyHeaderPositions(Sheet sheet){
        validColumnsList = new LinkedList<>();
        Row row =  getHeaderRow(sheet);
        assert row != null;
        for(int i = row.getFirstCellNum(); i<=row.getLastCellNum(); i++) {
            Cell c = row.getCell(i);
            if(isValidColumn(c)){
                validColumnsList.add(new ExclColumn(getCellValueAsString(c), i));
            }
        }
    }

    private boolean isValidColumn(Cell c){
        for(String validHeader:columnNames){
            if(validHeader.equals(getCellValueAsString(c))){
                return true;
            }
        }
        return false;
    }

    private Row getHeaderRow(Sheet sheet){
        int rowId = 0;
        for (Row row : sheet) {
            if(row.getFirstCellNum()>=0) {
                for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
                    if (isValidColumn(row.getCell(i))) {
                        return row;
                    }
                }
            }
            rowId++;
        }
        return null;
    }

    private boolean hasEmail(ControleCondominio cc) {
        return cc.getEmail().matches(".*@.*\\..*");
    }

    private void removerCabecalho(Sheet sheet) {
        Row headerRow = getHeaderRow(sheet);
        if(headerRow!=null) {
            int headerRowId = headerRow.getRowNum();
            for (int i = 0; i <= headerRowId; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }
        }else{
            throw new RuntimeException("Error. Couldn't read the file headers.");
        }
    }

    private String getStr(Row row, int index) {
        return row.getCell(index) != null ? row.getCell(index).getStringCellValue() : "";
    }

    private String getFrm(Row row, int index) {
        return row.getCell(index) != null ? row.getCell(index).getCellFormula() : "";
    }

    private Logger getLogger(){
        return LogManager.getLogger(ExcelReader.class);
    }
}
