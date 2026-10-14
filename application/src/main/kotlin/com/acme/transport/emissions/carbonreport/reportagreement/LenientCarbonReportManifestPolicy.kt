package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun lenientCarbonReportManifestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a lenient carbon report manifest code in its canonical LCRM- form. */
fun normalizeLenientCarbonReportManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCRM-")) {
        return clean
    }
    return "LCRM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCarbonReportManifest(): Int {
    val policy = defaultPolicy("lenient-carbon-report-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
