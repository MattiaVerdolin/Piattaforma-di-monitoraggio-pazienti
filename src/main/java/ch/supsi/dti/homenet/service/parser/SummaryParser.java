package ch.supsi.dti.homenet.service.parser;

import java.util.List;
import java.util.Map;

public interface SummaryParser {
    boolean supports(String summaryType);
    void parse(List<Map<String, Object>> data);
}
