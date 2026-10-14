package com.acme.transport.crewing.roster.rosterrule

fun seasonalRosterTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 79608.0, in order. */
fun seasonalRosterTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79608.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
