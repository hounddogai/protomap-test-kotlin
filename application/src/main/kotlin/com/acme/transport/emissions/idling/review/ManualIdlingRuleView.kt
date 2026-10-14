package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual idling rule. */
data class ManualIdlingRuleView(
    var codeText: String,
    var titleText: String,
    var trailerIdText: String,
    var contactEmailText: String,
    var rate: String,
    var pinned: String,
    var name: String,
    var statusLabel: String,
)

fun toManualIdlingRuleView(item: ManualIdlingRule): ManualIdlingRuleView {
    return ManualIdlingRuleView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        trailerIdText = truncate(item.trailerId, 64),
        contactEmailText = item.contactEmail,
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        name = truncate(item.name, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualIdlingRuleViews(items: MutableList<ManualIdlingRule>): MutableList<ManualIdlingRuleView> {
    val views = mutableListOf<ManualIdlingRuleView>()
    for (item in items) {
        views.add(toManualIdlingRuleView(item))
    }
    return views
}

fun sumManualIdlingRuleView(view: ManualIdlingRuleView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
