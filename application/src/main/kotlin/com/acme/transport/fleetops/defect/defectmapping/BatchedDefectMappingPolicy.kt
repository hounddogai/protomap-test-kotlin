package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDefectMapping(): Int {
    val policy = defaultPolicy("batched-defect-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched defect mapping code in its canonical BDM- form. */
fun normalizeBatchedDefectMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDM-")) {
        return clean
    }
    return "BDM-${clean.replace(" ", "-")}"
}

fun batchedDefectMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 34377.0, in order. */
fun batchedDefectMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34377.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
