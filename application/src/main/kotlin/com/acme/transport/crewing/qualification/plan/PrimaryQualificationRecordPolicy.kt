package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePrimaryQualificationRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary qualification record code in its canonical PQR- form. */
fun normalizePrimaryQualificationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PQR-")) {
        return clean
    }
    return "PQR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 83972.0, in order. */
fun primaryQualificationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83972.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryQualificationRecordBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
