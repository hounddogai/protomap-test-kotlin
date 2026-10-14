package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualInspectionTemplate(): Int {
    val policy = defaultPolicy("manual-inspection-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeManualInspectionTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual inspection template code in its canonical MIT- form. */
fun normalizeManualInspectionTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MIT-")) {
        return clean
    }
    return "MIT-${clean.replace(" ", "-")}"
}
