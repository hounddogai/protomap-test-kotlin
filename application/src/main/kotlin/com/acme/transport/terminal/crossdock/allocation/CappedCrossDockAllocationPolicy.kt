package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 15838.0, in order. */
fun cappedCrossDockAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15838.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCappedCrossDockAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedCrossDockAllocation(): Int {
    val policy = defaultPolicy("capped-cross-dock-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped cross dock allocation code in its canonical CCDA- form. */
fun normalizeCappedCrossDockAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCDA-")) {
        return clean
    }
    return "CCDA-${clean.replace(" ", "-")}"
}
