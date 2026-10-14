package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationDraft(): Int {
    val policy = defaultPolicy("declaration-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a declaration draft code in its canonical DD- form. */
fun normalizeDeclarationDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DD-")) {
        return clean
    }
    return "DD-${clean.replace(" ", "-")}"
}
