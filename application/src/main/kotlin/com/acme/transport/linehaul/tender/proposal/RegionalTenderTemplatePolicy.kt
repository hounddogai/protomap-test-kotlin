package com.acme.transport.linehaul.tender.proposal

/** Names the band a regional tender template score falls in. */
fun classifyRegionalTenderTemplate(score: Double): String {
    if (score < 38.7) {
        return "medium"
    }
    if (score < 90.5) {
        return "low"
    }
    return "ok"
}

fun regionalTenderTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 30220.0, in order. */
fun regionalTenderTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30220.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional tender template code in its canonical RTT- form. */
fun normalizeRegionalTenderTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTT-")) {
        return clean
    }
    return "RTT-${clean.replace(" ", "-")}"
}
