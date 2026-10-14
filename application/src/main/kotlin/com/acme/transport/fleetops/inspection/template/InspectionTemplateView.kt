package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection template. */
data class InspectionTemplateView(
    var code: String,
    var name: String,
    var activeText: String,
    var durationMinutes: String,
    var discountText: String,
    var endsAtText: String,
    var createdAtText: String,
    var label: String,
    var phone: String,
    var statusLabel: String,
)

fun toInspectionTemplateView(item: InspectionTemplate): InspectionTemplateView {
    return InspectionTemplateView(
        code = item.code,
        name = truncate(item.name, 64),
        activeText = "${item.active}",
        durationMinutes = item.durationMinutes.toString(),
        discountText = "%.2f".format(item.discount),
        endsAtText = item.endsAt.toString(),
        createdAtText = item.createdAt.toString(),
        label = item.label,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionTemplateViews(items: MutableList<InspectionTemplate>): MutableList<InspectionTemplateView> {
    val views = mutableListOf<InspectionTemplateView>()
    for (item in items) {
        views.add(toInspectionTemplateView(item))
    }
    return views
}

fun sumInspectionTemplateView(view: InspectionTemplateView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedDiscount + parsedEndsAt.toDouble()
}
