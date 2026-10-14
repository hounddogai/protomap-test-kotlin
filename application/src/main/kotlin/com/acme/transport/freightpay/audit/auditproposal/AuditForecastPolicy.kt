package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a audit forecast code in its canonical AF- form. */
fun normalizeAuditForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AF-")) {
        return clean
    }
    return "AF-${clean.replace(" ", "-")}"
}

fun mergeAuditForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
