package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRosterEstimate(): Int {
    val policy = defaultPolicy("seasonal-roster-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal roster estimate code in its canonical SRE- form. */
fun normalizeSeasonalRosterEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRE-")) {
        return clean
    }
    return "SRE-${clean.replace(" ", "-")}"
}

fun mergeSeasonalRosterEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
