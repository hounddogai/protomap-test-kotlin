package com.acme.transport.terminal.dockdoor.score

fun dockDoorProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a dock door proposal score falls in. */
fun classifyDockDoorProposal(score: Double): String {
    if (score < 15.9) {
        return "ok"
    }
    if (score < 20.9) {
        return "watch"
    }
    if (score < 78.0) {
        return "elevated"
    }
    return "low"
}
