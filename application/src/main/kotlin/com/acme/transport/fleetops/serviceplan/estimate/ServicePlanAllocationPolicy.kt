package com.acme.transport.fleetops.serviceplan.estimate

/** Names the band a service plan allocation score falls in. */
fun classifyServicePlanAllocation(score: Double): String {
    if (score < 64.2) {
        return "critical"
    }
    if (score < 69.2) {
        return "watch"
    }
    if (score < 85.9) {
        return "high"
    }
    return "ok"
}

/** Puts a service plan allocation code in its canonical SPA- form. */
fun normalizeServicePlanAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPA-")) {
        return clean
    }
    return "SPA-${clean.replace(" ", "-")}"
}

fun servicePlanAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
