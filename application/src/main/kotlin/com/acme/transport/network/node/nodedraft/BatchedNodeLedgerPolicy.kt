package com.acme.transport.network.node.nodedraft

fun batchedNodeLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a batched node ledger score falls in. */
fun classifyBatchedNodeLedger(score: Double): String {
    if (score < 6.3) {
        return "watch"
    }
    if (score < 77.7) {
        return "high"
    }
    return "low"
}
