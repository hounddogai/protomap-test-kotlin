package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeLegacyDefectSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDefectSnapshot(): Int {
    val policy = defaultPolicy("legacy-defect-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy defect snapshot code in its canonical LDS- form. */
fun normalizeLegacyDefectSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDS-")) {
        return clean
    }
    return "LDS-${clean.replace(" ", "-")}"
}
