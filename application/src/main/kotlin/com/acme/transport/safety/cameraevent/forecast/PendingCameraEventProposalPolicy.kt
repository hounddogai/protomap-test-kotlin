package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending camera event proposal code in its canonical PCEP- form. */
fun normalizePendingCameraEventProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCEP-")) {
        return clean
    }
    return "PCEP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingCameraEventProposal(): Int {
    val policy = defaultPolicy("pending-camera-event-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
