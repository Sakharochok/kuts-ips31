package com.dance.parser;

import com.dance.model.Dance;
import java.io.File;
import java.util.List;

public interface DanceParser {
    List<Dance> parse(File xmlFile);
}
