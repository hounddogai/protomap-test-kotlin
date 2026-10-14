package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive qualification assignment code in its canonical AQA- form. */
fun normalizeAdaptiveQualificationAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AQA-")) {
        return clean
    }
    return "AQA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveQualificationAssignment(): Int {
    val policy = defaultPolicy("adaptive-qualification-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
