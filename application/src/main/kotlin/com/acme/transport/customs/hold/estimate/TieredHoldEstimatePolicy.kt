package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a tiered hold estimate code in its canonical THE- form. */
fun normalizeTieredHoldEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("THE-")) {
        return clean
    }
    return "THE-${clean.replace(" ", "-")}"
}

fun mergeTieredHoldEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredHoldEstimate(): Int {
    val policy = defaultPolicy("tiered-hold-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
