package com.acme.transport.fleetops.defect.cycle

/** Puts a legacy defect cycle code in its canonical LDC- form. */
fun normalizeLegacyDefectCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDC-")) {
        return clean
    }
    return "LDC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 77115.0, in order. */
fun legacyDefectCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77115.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy defect cycle score falls in. */
fun classifyLegacyDefectCycle(score: Double): String {
    if (score < 46.3) {
        return "low"
    }
    if (score < 85.0) {
        return "elevated"
    }
    return "ok"
}

fun legacyDefectCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
