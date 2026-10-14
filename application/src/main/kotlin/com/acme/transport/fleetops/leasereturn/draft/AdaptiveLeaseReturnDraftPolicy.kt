package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveLeaseReturnDraft(): Int {
    val policy = defaultPolicy("adaptive-lease-return-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive lease return draft code in its canonical ALRD- form. */
fun normalizeAdaptiveLeaseReturnDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ALRD-")) {
        return clean
    }
    return "ALRD-${clean.replace(" ", "-")}"
}

fun adaptiveLeaseReturnDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveLeaseReturnDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
