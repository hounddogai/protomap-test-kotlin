package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedParcelDigest(): Int {
    val policy = defaultPolicy("capped-parcel-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped parcel digest code in its canonical CPD- form. */
fun normalizeCappedParcelDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CPD-")) {
        return clean
    }
    return "CPD-${clean.replace(" ", "-")}"
}
