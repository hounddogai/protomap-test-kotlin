package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCarbonReportVariance(): Int {
    val policy = defaultPolicy("tiered-carbon-report-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredCarbonReportVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered carbon report variance code in its canonical TCRV- form. */
fun normalizeTieredCarbonReportVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCRV-")) {
        return clean
    }
    return "TCRV-${clean.replace(" ", "-")}"
}

fun mergeTieredCarbonReportVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
