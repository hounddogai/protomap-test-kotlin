package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryDefectBundle(): Int {
    val policy = defaultPolicy("primary-defect-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryDefectBundleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a primary defect bundle code in its canonical PDB- form. */
fun normalizePrimaryDefectBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDB-")) {
        return clean
    }
    return "PDB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 47178.0, in order. */
fun primaryDefectBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47178.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
