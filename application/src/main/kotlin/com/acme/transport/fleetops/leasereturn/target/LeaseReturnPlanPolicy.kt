package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a lease return plan code in its canonical LRP- form. */
fun normalizeLeaseReturnPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRP-")) {
        return clean
    }
    return "LRP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnPlan(): Int {
    val policy = defaultPolicy("lease-return-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLeaseReturnPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lease return plan score falls in. */
fun classifyLeaseReturnPlan(score: Double): String {
    if (score < 1.6) {
        return "medium"
    }
    if (score < 42.8) {
        return "ok"
    }
    if (score < 89.4) {
        return "watch"
    }
    return "elevated"
}
