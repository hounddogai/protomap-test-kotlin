package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching template. */
data class CoachingTemplateView(
    var code: String,
    var label: String,
    var capacityText: String,
    var version: String,
    var ownerId: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toCoachingTemplateView(item: CoachingTemplate): CoachingTemplateView {
    return CoachingTemplateView(
        code = item.code,
        label = truncate(item.label, 40),
        capacityText = item.capacity.toString(),
        version = item.version.toString(),
        ownerId = item.ownerId,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingTemplateViews(items: MutableList<CoachingTemplate>): MutableList<CoachingTemplateView> {
    val views = mutableListOf<CoachingTemplateView>()
    for (item in items) {
        views.add(toCoachingTemplateView(item))
    }
    return views
}

fun sumCoachingTemplateView(view: CoachingTemplateView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedCapacity.toDouble() + parsedVersion.toDouble()
}
