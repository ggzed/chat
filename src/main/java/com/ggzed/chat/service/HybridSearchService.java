package com.ggzed.chat.service;

import java.io.IOException;
import java.util.List;

public interface HybridSearchService {
    List<String> hybridSearch(String query, int topN) throws IOException;
}
