package com.acme.transport.customs.declaration.audit

fun regionalDeclarationAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a regional declaration audit score falls in. */
fun classifyRegionalDeclarationAudit(score: Double): String {
    if (score < 13.4) {
        return "minimal"
    }
    if (score < 44.3) {
        return "elevated"
    }
    if (score < 70.5) {
        return "low"
    }
    if (score < 82.7) {
        return "medium"
    }
    return "ok"
}
