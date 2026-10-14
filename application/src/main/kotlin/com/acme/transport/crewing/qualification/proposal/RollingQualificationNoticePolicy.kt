package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rollingQualificationNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun rollingQualificationNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingQualificationNotice(): Int {
    val policy = defaultPolicy("rolling-qualification-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rolling qualification notice code in its canonical RQN- form. */
fun normalizeRollingQualificationNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQN-")) {
        return clean
    }
    return "RQN-${clean.replace(" ", "-")}"
}
