package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default proof of delivery template code in its canonical DPODT- form. */
fun normalizeDefaultProofOfDeliveryTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPODT-")) {
        return clean
    }
    return "DPODT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultProofOfDeliveryTemplate(): Int {
    val policy = defaultPolicy("default-proof-of-delivery-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 16352.0, in order. */
fun defaultProofOfDeliveryTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16352.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
