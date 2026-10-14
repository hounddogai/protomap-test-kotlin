package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

/** Puts a tiered proof of delivery rollup code in its canonical TPODR- form. */
fun normalizeTieredProofOfDeliveryRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TPODR-")) {
        return clean
    }
    return "TPODR-${clean.replace(" ", "-")}"
}

fun tieredProofOfDeliveryRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
