package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedInspectionAllocation(): Int {
    val policy = defaultPolicy("weighted-inspection-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted inspection allocation code in its canonical WIA- form. */
fun normalizeWeightedInspectionAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WIA-")) {
        return clean
    }
    return "WIA-${clean.replace(" ", "-")}"
}

fun mergeWeightedInspectionAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedInspectionAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
