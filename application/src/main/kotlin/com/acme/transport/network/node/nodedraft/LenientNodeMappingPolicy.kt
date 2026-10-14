package com.acme.transport.network.node.nodedraft

/** Puts a lenient node mapping code in its canonical LNM- form. */
fun normalizeLenientNodeMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNM-")) {
        return clean
    }
    return "LNM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 74622.0, in order. */
fun lenientNodeMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74622.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
