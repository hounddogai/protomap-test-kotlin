package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched cross dock proposal code in its canonical BCDP- form. */
fun normalizeBatchedCrossDockProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BCDP-")) {
        return clean
    }
    return "BCDP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedCrossDockProposal(): Int {
    val policy = defaultPolicy("batched-cross-dock-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
