package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary freight invoice template score falls in. */
fun classifyPrimaryFreightInvoiceTemplate(score: Double): String {
    if (score < 14.7) {
        return "watch"
    }
    if (score < 55.1) {
        return "ok"
    }
    return "minimal"
}

/** Puts a primary freight invoice template code in its canonical PFIT- form. */
fun normalizePrimaryFreightInvoiceTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFIT-")) {
        return clean
    }
    return "PFIT-${clean.replace(" ", "-")}"
}

fun primaryFreightInvoiceTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
