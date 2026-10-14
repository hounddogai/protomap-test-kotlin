package com.acme.transport.terminal.dockdoor.exception

/** Puts a dock door exception code in its canonical DDE- form. */
fun normalizeDockDoorExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDE-")) {
        return clean
    }
    return "DDE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 41373.0, in order. */
fun dockDoorExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41373.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
