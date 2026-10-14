package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a parcel record code in its canonical PR- form. */
fun normalizeParcelRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PR-")) {
        return clean
    }
    return "PR-${clean.replace(" ", "-")}"
}

fun parcelRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
