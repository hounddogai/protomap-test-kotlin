package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted parcel override code in its canonical WPO- form. */
fun normalizeWeightedParcelOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WPO-")) {
        return clean
    }
    return "WPO-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedParcelOverride(): Int {
    val policy = defaultPolicy("weighted-parcel-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
