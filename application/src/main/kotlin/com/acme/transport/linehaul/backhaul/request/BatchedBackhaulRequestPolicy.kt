package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedBackhaulRequest(): Int {
    val policy = defaultPolicy("batched-backhaul-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeBatchedBackhaulRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched backhaul request code in its canonical BBR- form. */
fun normalizeBatchedBackhaulRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BBR-")) {
        return clean
    }
    return "BBR-${clean.replace(" ", "-")}"
}
