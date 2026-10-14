package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeScheduledLeaseReturnAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled lease return assignment code in its canonical SLRA- form. */
fun normalizeScheduledLeaseReturnAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLRA-")) {
        return clean
    }
    return "SLRA-${clean.replace(" ", "-")}"
}
