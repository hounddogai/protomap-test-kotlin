package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedFuelBurnBudget(): Int {
    val policy = defaultPolicy("weighted-fuel-burn-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeWeightedFuelBurnBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted fuel burn budget code in its canonical WFBB- form. */
fun normalizeWeightedFuelBurnBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFBB-")) {
        return clean
    }
    return "WFBB-${clean.replace(" ", "-")}"
}
