package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a parcel profile code in its canonical PP- form. */
fun normalizeParcelProfileCode(code: String): String {
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
fun probeParcelProfile(): Int {
    val policy = defaultPolicy("parcel-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun parcelProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
