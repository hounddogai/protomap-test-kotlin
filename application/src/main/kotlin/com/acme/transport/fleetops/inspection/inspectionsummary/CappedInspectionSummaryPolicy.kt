package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedInspectionSummary(): Int {
    val policy = defaultPolicy("capped-inspection-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped inspection summary score falls in. */
fun classifyCappedInspectionSummary(score: Double): String {
    if (score < 55.8) {
        return "critical"
    }
    if (score < 69.5) {
        return "low"
    }
    return "minimal"
}

fun mergeCappedInspectionSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedInspectionSummaryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
