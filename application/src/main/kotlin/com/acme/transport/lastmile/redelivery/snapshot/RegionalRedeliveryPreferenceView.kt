package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional redelivery preference. */
data class RegionalRedeliveryPreferenceView(
    var codeText: String,
    var name: String,
    var ratioText: String,
    var amount: String,
    var endsAt: String,
    var notesText: String,
    var category: String,
    var loadKg: String,
    var palletsText: String,
    var limit: String,
    var statusLabel: String,
)

fun toRegionalRedeliveryPreferenceView(item: RegionalRedeliveryPreference): RegionalRedeliveryPreferenceView {
    return RegionalRedeliveryPreferenceView(
        codeText = item.code,
        name = item.name,
        ratioText = "%.2f".format(item.ratio),
        amount = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        notesText = item.notes,
        category = truncate(item.category, 64),
        loadKg = "%.2f".format(item.loadKg),
        palletsText = item.pallets.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRedeliveryPreferenceViews(items: MutableList<RegionalRedeliveryPreference>): MutableList<RegionalRedeliveryPreferenceView> {
    val views = mutableListOf<RegionalRedeliveryPreferenceView>()
    for (item in items) {
        views.add(toRegionalRedeliveryPreferenceView(item))
    }
    return views
}

fun sumRegionalRedeliveryPreferenceView(view: RegionalRedeliveryPreferenceView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRatio + parsedAmount + parsedEndsAt.toDouble()
}
