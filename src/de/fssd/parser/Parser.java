package de.fssd.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fssd.dataobjects.FaultTree;

import java.io.*;

/**
 * Created by Andre on 16.06.2016.
 */
public class Parser {
    private Gson gson = new GsonBuilder().create();

    public FaultTree parse(File fromFile) throws IOException {
        InputStreamReader reader;
        if (fromFile == null)
            reader = new InputStreamReader(System.in);
        else
            reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(fromFile)));
        return gson.fromJson(reader, FaultTree.class);
    }
}
