package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalQualificationNoticeBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalQualificationNotice(): Int {
    val policy = defaultPolicy("regional-qualification-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional qualification notice code in its canonical RQN- form. */
fun normalizeRegionalQualificationNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQN-")) {
        return clean
    }
    return "RQN-${clean.replace(" ", "-")}"
}
