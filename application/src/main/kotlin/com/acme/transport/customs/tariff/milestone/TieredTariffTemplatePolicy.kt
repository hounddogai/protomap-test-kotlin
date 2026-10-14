package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 63808.0, in order. */
fun tieredTariffTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63808.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredTariffTemplateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a tiered tariff template code in its canonical TTT- form. */
fun normalizeTieredTariffTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTT-")) {
        return clean
    }
    return "TTT-${clean.replace(" ", "-")}"
}
