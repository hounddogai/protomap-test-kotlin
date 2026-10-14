package com.acme.transport.lastmile.parcel.segment

fun parcelDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a parcel digest score falls in. */
fun classifyParcelDigest(score: Double): String {
    if (score < 56.3) {
        return "elevated"
    }
    if (score < 65.5) {
        return "ok"
    }
    return "low"
}
