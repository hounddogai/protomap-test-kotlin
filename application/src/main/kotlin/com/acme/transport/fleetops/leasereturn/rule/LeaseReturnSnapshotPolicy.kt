package com.acme.transport.fleetops.leasereturn.rule

/** Counts how many amounts fit in a budget of 41034.0, in order. */
fun leaseReturnSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41034.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lease return snapshot code in its canonical LRS- form. */
fun normalizeLeaseReturnSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRS-")) {
        return clean
    }
    return "LRS-${clean.replace(" ", "-")}"
}
