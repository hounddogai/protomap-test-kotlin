package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 85277.0, in order. */
fun tenderSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85277.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tender slot code in its canonical TS- form. */
fun normalizeTenderSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TS-")) {
        return clean
    }
    return "TS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTenderSlot(): Int {
    val policy = defaultPolicy("tender-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
