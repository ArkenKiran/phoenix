package org.apache.phoenix.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.phoenix.query.QueryServices;
import org.slf4j.MDC;

public class QueryIdentifierUtil {
  public static String QUERY_IDENTIFIER_KEY = "queryId";

  public static void setQuerIDInMDC(Configuration configuration, String queryId) {
    if (configuration.getBoolean(QueryServices.PHOENIX_QUERY_IDENTIFIER_LOGGING_ENABLED, false)
        && queryId != null) {
      MDC.put(QUERY_IDENTIFIER_KEY, queryId);
    }
  }

  public static void removeQuerIDInMDC(Configuration configuration) {
    if (configuration.getBoolean(QueryServices.PHOENIX_QUERY_IDENTIFIER_LOGGING_ENABLED, false)) {
      MDC.remove(QUERY_IDENTIFIER_KEY);
    }
  }
}
