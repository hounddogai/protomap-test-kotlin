package com.acme.transport.terminal.dockdoor.rule

fun dockDoorRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a dock door rule score falls in. */
fun classifyDockDoorRule(score: Double): String {
    if (score < 19.3) {
        return "elevated"
    }
    if (score < 26.0) {
        return "minimal"
    }
    if (score < 73.5) {
        return "high"
    }
    if (score < 95.7) {
        return "medium"
    }
    return "ok"
}
