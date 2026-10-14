package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a batched proof of delivery manifest code in its canonical BPODM- form. */
fun normalizeBatchedProofOfDeliveryManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPODM-")) {
        return clean
    }
    return "BPODM-${clean.replace(" ", "-")}"
}

fun batchedProofOfDeliveryManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun batchedProofOfDeliveryManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
