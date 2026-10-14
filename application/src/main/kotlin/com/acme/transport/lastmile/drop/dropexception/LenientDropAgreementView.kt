package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient drop agreement. */
data class LenientDropAgreementView(
    var codeText: String,
    var labelText: String,
    var windowDays: String,
    var summary: String,
    var unit: String,
    var statusLabel: String,
)

fun toLenientDropAgreementView(item: LenientDropAgreement): LenientDropAgreementView {
    return LenientDropAgreementView(
        codeText = truncate(item.code, 40),
        labelText = item.label,
        windowDays = item.windowDays.toString(),
        summary = item.summary,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDropAgreementViews(items: MutableList<LenientDropAgreement>): MutableList<LenientDropAgreementView> {
    val views = mutableListOf<LenientDropAgreementView>()
    for (item in items) {
        views.add(toLenientDropAgreementView(item))
    }
    return views
}

fun sumLenientDropAgreementView(view: LenientDropAgreementView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
