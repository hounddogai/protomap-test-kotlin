package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 46517.0, in order. */
fun batchedParcelTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46517.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedParcelTemplate(): Int {
    val policy = defaultPolicy("batched-parcel-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched parcel template code in its canonical BPT- form. */
fun normalizeBatchedParcelTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPT-")) {
        return clean
    }
    return "BPT-${clean.replace(" ", "-")}"
}
