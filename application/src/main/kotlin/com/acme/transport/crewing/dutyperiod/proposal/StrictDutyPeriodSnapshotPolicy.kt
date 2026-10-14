package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun strictDutyPeriodSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 21943.0, in order. */
fun strictDutyPeriodSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21943.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeStrictDutyPeriodSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a strict duty period snapshot code in its canonical SDPS- form. */
fun normalizeStrictDutyPeriodSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDPS-")) {
        return clean
    }
    return "SDPS-${clean.replace(" ", "-")}"
}
