package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 6279.0, in order. */
fun seasonalCarbonReportAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6279.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalCarbonReportAgreement(): Int {
    val policy = defaultPolicy("seasonal-carbon-report-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeSeasonalCarbonReportAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal carbon report agreement score falls in. */
fun classifySeasonalCarbonReportAgreement(score: Double): String {
    if (score < 11.3) {
        return "medium"
    }
    if (score < 18.5) {
        return "critical"
    }
    if (score < 25.8) {
        return "low"
    }
    return "minimal"
}
