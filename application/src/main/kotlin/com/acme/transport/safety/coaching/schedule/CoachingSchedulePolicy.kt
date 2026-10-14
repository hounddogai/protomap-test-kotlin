package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.numbers.roundTo

/** Puts a coaching schedule code in its canonical CS- form. */
fun normalizeCoachingScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}

fun coachingScheduleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
