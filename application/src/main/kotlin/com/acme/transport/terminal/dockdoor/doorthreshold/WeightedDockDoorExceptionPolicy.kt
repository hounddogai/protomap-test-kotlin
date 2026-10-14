package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted dock door exception code in its canonical WDDE- form. */
fun normalizeWeightedDockDoorExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDDE-")) {
        return clean
    }
    return "WDDE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 12755.0, in order. */
fun weightedDockDoorExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12755.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedDockDoorException(): Int {
    val policy = defaultPolicy("weighted-dock-door-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
