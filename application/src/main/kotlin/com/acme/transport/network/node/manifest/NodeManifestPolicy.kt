package com.acme.transport.network.node.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun nodeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a node manifest code in its canonical NM- form. */
fun normalizeNodeManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NM-")) {
        return clean
    }
    return "NM-${clean.replace(" ", "-")}"
}
