package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional drop preference. */
data class RegionalDropPreferenceView(
    var code: String,
    var label: String,
    var limit: String,
    var contactEmail: String,
    var amount: String,
    var visible: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toRegionalDropPreferenceView(item: RegionalDropPreference): RegionalDropPreferenceView {
    return RegionalDropPreferenceView(
        code = item.code,
        label = truncate(item.label, 64),
        limit = item.limit.toString(),
        contactEmail = item.contactEmail,
        amount = "%.2f".format(item.amount),
        visible = "${item.visible}",
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDropPreferenceViews(items: MutableList<RegionalDropPreference>): MutableList<RegionalDropPreferenceView> {
    val views = mutableListOf<RegionalDropPreferenceView>()
    for (item in items) {
        views.add(toRegionalDropPreferenceView(item))
    }
    return views
}

fun sumRegionalDropPreferenceView(view: RegionalDropPreferenceView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedLimit.toDouble() + parsedAmount + parsedExpiresAt.toDouble()
}
