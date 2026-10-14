package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

fun manualProofOfDeliveryRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a manual proof of delivery record code in its canonical MPODR- form. */
fun normalizeManualProofOfDeliveryRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MPODR-")) {
        return clean
    }
    return "MPODR-${clean.replace(" ", "-")}"
}
