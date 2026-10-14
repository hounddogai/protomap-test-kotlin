package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationBundle(): Int {
    val policy = defaultPolicy("sortation-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a sortation bundle code in its canonical SB- form. */
fun normalizeSortationBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SB-")) {
        return clean
    }
    return "SB-${clean.replace(" ", "-")}"
}
