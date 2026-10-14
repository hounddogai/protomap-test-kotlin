package com.acme.transport.terminal.sortation.sortationtemplate

/** Counts how many amounts fit in a budget of 61250.0, in order. */
fun primarySortationTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61250.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary sortation template code in its canonical PST- form. */
fun normalizePrimarySortationTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PST-")) {
        return clean
    }
    return "PST-${clean.replace(" ", "-")}"
}
