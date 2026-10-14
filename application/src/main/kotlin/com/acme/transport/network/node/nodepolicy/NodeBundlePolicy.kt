package com.acme.transport.network.node.nodepolicy

/** Names the band a node bundle score falls in. */
fun classifyNodeBundle(score: Double): String {
    if (score < 20.1) {
        return "minimal"
    }
    if (score < 30.9) {
        return "ok"
    }
    return "critical"
}

fun nodeBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
