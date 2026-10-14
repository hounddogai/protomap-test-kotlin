package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a parcel preference code in its canonical PP- form. */
fun normalizeParcelPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PP-")) {
        return clean
    }
    return "PP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelPreference(): Int {
    val policy = defaultPolicy("parcel-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
