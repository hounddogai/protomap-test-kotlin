package com.acme.transport.network.node.draft

/** Names the band a node reservation score falls in. */
fun classifyNodeReservation(score: Double): String {
    if (score < 2.9) {
        return "high"
    }
    if (score < 96.4) {
        return "elevated"
    }
    return "ok"
}

/** Puts a node reservation code in its canonical NR- form. */
fun normalizeNodeReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NR-")) {
        return clean
    }
    return "NR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 61794.0, in order. */
fun nodeReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61794.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun nodeReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
