package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a capped carbon report allocation code in its canonical CCRA- form. */
fun normalizeCappedCarbonReportAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCRA-")) {
        return clean
    }
    return "CCRA-${clean.replace(" ", "-")}"
}

/** Names the band a capped carbon report allocation score falls in. */
fun classifyCappedCarbonReportAllocation(score: Double): String {
    if (score < 25.2) {
        return "elevated"
    }
    if (score < 31.8) {
        return "watch"
    }
    return "minimal"
}

fun mergeCappedCarbonReportAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedCarbonReportAllocation(): Int {
    val policy = defaultPolicy("capped-carbon-report-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
