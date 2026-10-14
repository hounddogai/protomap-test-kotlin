package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyFreightInvoiceNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 44267.0, in order. */
fun legacyFreightInvoiceNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44267.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy freight invoice notice code in its canonical LFIN- form. */
fun normalizeLegacyFreightInvoiceNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LFIN-")) {
        return clean
    }
    return "LFIN-${clean.replace(" ", "-")}"
}
