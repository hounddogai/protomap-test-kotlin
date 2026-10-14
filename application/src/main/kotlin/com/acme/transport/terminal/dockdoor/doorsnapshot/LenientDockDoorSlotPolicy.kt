package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.numbers.roundTo

fun lenientDockDoorSlotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a lenient dock door slot code in its canonical LDDS- form. */
fun normalizeLenientDockDoorSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDDS-")) {
        return clean
    }
    return "LDDS-${clean.replace(" ", "-")}"
}
