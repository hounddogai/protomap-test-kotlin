package com.acme.transport.customs.clearance.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance preference. */
data class ClearancePreferenceView(
    var code: String,
    var nameText: String,
    var endsAt: String,
    var limit: String,
    var region: String,
    var locale: String,
    var capacity: String,
    var descriptionText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toClearancePreferenceView(item: ClearancePreference): ClearancePreferenceView {
    return ClearancePreferenceView(
        code = truncate(item.code, 64),
        nameText = item.name,
        endsAt = item.endsAt.toString(),
        limit = item.limit.toString(),
        region = truncate(item.region, 80),
        locale = item.locale,
        capacity = item.capacity.toString(),
        descriptionText = item.description,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearancePreferenceViews(items: MutableList<ClearancePreference>): MutableList<ClearancePreferenceView> {
    val views = mutableListOf<ClearancePreferenceView>()
    for (item in items) {
        views.add(toClearancePreferenceView(item))
    }
    return views
}

fun sumClearancePreferenceView(view: ClearancePreferenceView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedEndsAt.toDouble() + parsedLimit.toDouble() + parsedCapacity.toDouble()
}
