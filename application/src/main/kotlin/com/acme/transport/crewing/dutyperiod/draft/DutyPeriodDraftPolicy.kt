package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 68402.0, in order. */
fun dutyPeriodDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68402.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a duty period draft code in its canonical DPD- form. */
fun normalizeDutyPeriodDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPD-")) {
        return clean
    }
    return "DPD-${clean.replace(" ", "-")}"
}

fun mergeDutyPeriodDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dutyPeriodDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
