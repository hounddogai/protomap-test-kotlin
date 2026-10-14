package com.acme.transport.fleetops.leasereturn.budget

/** Counts how many amounts fit in a budget of 13373.0, in order. */
fun globalLeaseReturnAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13373.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global lease return assignment code in its canonical GLRA- form. */
fun normalizeGlobalLeaseReturnAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GLRA-")) {
        return clean
    }
    return "GLRA-${clean.replace(" ", "-")}"
}
