package com.acme.transport.linehaul.capacity.request

/** Names the band a capacity proposal score falls in. */
fun classifyCapacityProposal(score: Double): String {
    if (score < 19.1) {
        return "high"
    }
    if (score < 55.8) {
        return "low"
    }
    return "ok"
}

fun capacityProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
