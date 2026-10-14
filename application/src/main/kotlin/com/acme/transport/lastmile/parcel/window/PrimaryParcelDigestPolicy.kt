package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryParcelDigest(): Int {
    val policy = defaultPolicy("primary-parcel-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary parcel digest code in its canonical PPD- form. */
fun normalizePrimaryParcelDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PPD-")) {
        return clean
    }
    return "PPD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 26344.0, in order. */
fun primaryParcelDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26344.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
