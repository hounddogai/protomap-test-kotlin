package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.numbers.roundTo

fun parcelProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a parcel proposal code in its canonical PP- form. */
fun normalizeParcelProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PP-")) {
        return clean
    }
    return "PP-${clean.replace(" ", "-")}"
}

/** Names the band a parcel proposal score falls in. */
fun classifyParcelProposal(score: Double): String {
    if (score < 5.6) {
        return "watch"
    }
    if (score < 37.2) {
        return "medium"
    }
    return "elevated"
}
