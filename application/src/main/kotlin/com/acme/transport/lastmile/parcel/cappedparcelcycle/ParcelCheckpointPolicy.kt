package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a parcel checkpoint score falls in. */
fun classifyParcelCheckpoint(score: Double): String {
    if (score < 47.5) {
        return "critical"
    }
    if (score < 72.8) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelCheckpoint(): Int {
    val policy = defaultPolicy("parcel-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a parcel checkpoint code in its canonical PC- form. */
fun normalizeParcelCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PC-")) {
        return clean
    }
    return "PC-${clean.replace(" ", "-")}"
}
