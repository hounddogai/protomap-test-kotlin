package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEvChargeSlot(): Int {
    val policy = defaultPolicy("scheduled-ev-charge-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeScheduledEvChargeSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled ev charge slot code in its canonical SECS- form. */
fun normalizeScheduledEvChargeSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SECS-")) {
        return clean
    }
    return "SECS-${clean.replace(" ", "-")}"
}
