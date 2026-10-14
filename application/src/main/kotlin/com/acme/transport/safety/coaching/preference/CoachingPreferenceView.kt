package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching preference. */
data class CoachingPreferenceView(
    var code: String,
    var name: String,
    var discount: String,
    var capacity: String,
    var revision: String,
    var channel: String,
    var createdAt: String,
    var titleText: String,
    var statusLabel: String,
)

fun toCoachingPreferenceView(item: CoachingPreference): CoachingPreferenceView {
    return CoachingPreferenceView(
        code = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        revision = item.revision.toString(),
        channel = truncate(item.channel, 64),
        createdAt = item.createdAt.toString(),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingPreferenceViews(items: MutableList<CoachingPreference>): MutableList<CoachingPreferenceView> {
    val views = mutableListOf<CoachingPreferenceView>()
    for (item in items) {
        views.add(toCoachingPreferenceView(item))
    }
    return views
}

fun sumCoachingPreferenceView(view: CoachingPreferenceView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedDiscount + parsedCapacity.toDouble() + parsedRevision.toDouble()
}
