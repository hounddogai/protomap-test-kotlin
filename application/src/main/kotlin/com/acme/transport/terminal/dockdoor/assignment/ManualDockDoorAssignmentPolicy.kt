package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDockDoorAssignment(): Int {
    val policy = defaultPolicy("manual-dock-door-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual dock door assignment code in its canonical MDDA- form. */
fun normalizeManualDockDoorAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDDA-")) {
        return clean
    }
    return "MDDA-${clean.replace(" ", "-")}"
}

fun manualDockDoorAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
