package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeBrokerSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a broker snapshot code in its canonical BS- form. */
fun normalizeBrokerSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BS-")) {
        return clean
    }
    return "BS-${clean.replace(" ", "-")}"
}

/** Names the band a broker snapshot score falls in. */
fun classifyBrokerSnapshot(score: Double): String {
    if (score < 5.8) {
        return "critical"
    }
    if (score < 22.5) {
        return "medium"
    }
    if (score < 39.0) {
        return "high"
    }
    if (score < 41.0) {
        return "ok"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerSnapshot(): Int {
    val policy = defaultPolicy("broker-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
