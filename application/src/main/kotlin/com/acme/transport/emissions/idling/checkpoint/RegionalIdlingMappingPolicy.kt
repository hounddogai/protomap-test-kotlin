package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRegionalIdlingMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalIdlingMapping(): Int {
    val policy = defaultPolicy("regional-idling-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional idling mapping code in its canonical RIM- form. */
fun normalizeRegionalIdlingMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIM-")) {
        return clean
    }
    return "RIM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 57089.0, in order. */
fun regionalIdlingMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57089.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
