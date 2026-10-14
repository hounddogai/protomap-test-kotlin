package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a primary capacity variance code in its canonical PCV- form. */
fun normalizePrimaryCapacityVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCV-")) {
        return clean
    }
    return "PCV-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCapacityVariance(): Int {
    val policy = defaultPolicy("primary-capacity-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 77798.0, in order. */
fun primaryCapacityVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77798.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryCapacityVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
