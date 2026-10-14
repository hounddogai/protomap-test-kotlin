package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict cross dock agreement. */
data class StrictCrossDockAgreementView(
    var code: String,
    var title: String,
    var lane: String,
    var currency: String,
    var priority: String,
    var statusLabel: String,
)

fun toStrictCrossDockAgreementView(item: StrictCrossDockAgreement): StrictCrossDockAgreementView {
    return StrictCrossDockAgreementView(
        code = item.code,
        title = item.title,
        lane = truncate(item.lane, 40),
        currency = item.currency,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCrossDockAgreementViews(items: MutableList<StrictCrossDockAgreement>): MutableList<StrictCrossDockAgreementView> {
    val views = mutableListOf<StrictCrossDockAgreementView>()
    for (item in items) {
        views.add(toStrictCrossDockAgreementView(item))
    }
    return views
}

fun sumStrictCrossDockAgreementView(view: StrictCrossDockAgreementView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
