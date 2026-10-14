package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingCarbonReportWindow(): Int {
    val policy = defaultPolicy("pending-carbon-report-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending carbon report window code in its canonical PCRW- form. */
fun normalizePendingCarbonReportWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCRW-")) {
        return clean
    }
    return "PCRW-${clean.replace(" ", "-")}"
}

fun mergePendingCarbonReportWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingCarbonReportWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
