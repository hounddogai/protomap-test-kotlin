package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a scheduled fuel surcharge window code in its canonical SFSW- form. */
fun normalizeScheduledFuelSurchargeWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFSW-")) {
        return clean
    }
    return "SFSW-${clean.replace(" ", "-")}"
}

fun mergeScheduledFuelSurchargeWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
