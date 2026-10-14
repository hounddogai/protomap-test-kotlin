package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a parcel draft code in its canonical PD- form. */
fun normalizeParcelDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PD-")) {
        return clean
    }
    return "PD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 60204.0, in order. */
fun parcelDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60204.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelDraft(): Int {
    val policy = defaultPolicy("parcel-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
