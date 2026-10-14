package com.acme.transport.terminal.dockdoor.exception

/** Counts how many amounts fit in a budget of 77865.0, in order. */
fun scheduledDockDoorNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77865.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled dock door notice code in its canonical SDDN- form. */
fun normalizeScheduledDockDoorNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDN-")) {
        return clean
    }
    return "SDDN-${clean.replace(" ", "-")}"
}
