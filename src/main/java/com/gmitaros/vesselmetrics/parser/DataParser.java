package com.gmitaros.vesselmetrics.parser;


import java.io.InputStream;

public interface DataParser {

    void parseAndSave(InputStream inputStream);

}
