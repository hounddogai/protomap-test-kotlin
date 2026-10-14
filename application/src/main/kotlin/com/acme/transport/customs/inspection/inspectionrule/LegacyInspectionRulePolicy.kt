package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyInspectionRule(): Int {
    val policy = defaultPolicy("legacy-inspection-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLegacyInspectionRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy inspection rule score falls in. */
fun classifyLegacyInspectionRule(score: Double): String {
    if (score < 67.0) {
        return "low"
    }
    if (score < 78.1) {
        return "elevated"
    }
    return "minimal"
}
