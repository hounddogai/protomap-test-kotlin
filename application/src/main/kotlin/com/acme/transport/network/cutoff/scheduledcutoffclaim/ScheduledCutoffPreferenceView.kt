package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled cutoff preference. */
data class ScheduledCutoffPreferenceView(
    var code: String,
    var titleText: String,
    var locale: String,
    var sequence: String,
    var descriptionText: String,
    var updatedAt: String,
    var discount: String,
    var category: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toScheduledCutoffPreferenceView(item: ScheduledCutoffPreference): ScheduledCutoffPreferenceView {
    return ScheduledCutoffPreferenceView(
        code = item.code,
        titleText = item.title,
        locale = item.locale,
        sequence = item.sequence.toString(),
        descriptionText = truncate(item.description, 64),
        updatedAt = item.updatedAt.toString(),
        discount = "%.2f".format(item.discount),
        category = item.category,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCutoffPreferenceViews(items: MutableList<ScheduledCutoffPreference>): MutableList<ScheduledCutoffPreferenceView> {
    val views = mutableListOf<ScheduledCutoffPreferenceView>()
    for (item in items) {
        views.add(toScheduledCutoffPreferenceView(item))
    }
    return views
}

fun sumScheduledCutoffPreferenceView(view: ScheduledCutoffPreferenceView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedSequence.toDouble() + parsedUpdatedAt.toDouble() + parsedDiscount
}
