package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a carbon report mapping code in its canonical CRM- form. */
fun normalizeCarbonReportMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRM-")) {
        return clean
    }
    return "CRM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCarbonReportMapping(): Int {
    val policy = defaultPolicy("carbon-report-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun carbonReportMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
