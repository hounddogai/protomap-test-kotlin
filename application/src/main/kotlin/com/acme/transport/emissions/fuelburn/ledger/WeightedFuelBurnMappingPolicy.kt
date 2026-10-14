package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedFuelBurnMapping(): Int {
    val policy = defaultPolicy("weighted-fuel-burn-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted fuel burn mapping code in its canonical WFBM- form. */
fun normalizeWeightedFuelBurnMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFBM-")) {
        return clean
    }
    return "WFBM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 58368.0, in order. */
fun weightedFuelBurnMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58368.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeWeightedFuelBurnMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
