package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualDutyPeriodLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual duty period ledger score falls in. */
fun classifyManualDutyPeriodLedger(score: Double): String {
    if (score < 22.6) {
        return "elevated"
    }
    if (score < 46.7) {
        return "medium"
    }
    if (score < 70.2) {
        return "critical"
    }
    if (score < 91.3) {
        return "watch"
    }
    return "low"
}

/** Puts a manual duty period ledger code in its canonical MDPL- form. */
fun normalizeManualDutyPeriodLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDPL-")) {
        return clean
    }
    return "MDPL-${clean.replace(" ", "-")}"
}
