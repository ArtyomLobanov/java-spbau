package ru.spbau.lobanov.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamSerializable {

    void serialize(OutputStream out) throws IOException;

    /**
     * Replace current state with data from input stream
     */
    void deserialize(InputStream in) throws IOException;
}