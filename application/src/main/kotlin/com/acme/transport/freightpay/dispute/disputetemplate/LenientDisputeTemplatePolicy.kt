package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 5156.0, in order. */
fun lenientDisputeTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5156.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLenientDisputeTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient dispute template code in its canonical LDT- form. */
fun normalizeLenientDisputeTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDT-")) {
        return clean
    }
    return "LDT-${clean.replace(" ", "-")}"
}
