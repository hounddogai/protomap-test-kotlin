package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling template. */
data class IdlingTemplateView(
    var codeText: String,
    var label: String,
    var quantity: String,
    var durationMinutes: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toIdlingTemplateView(item: IdlingTemplate): IdlingTemplateView {
    return IdlingTemplateView(
        codeText = truncate(item.code, 40),
        label = item.label,
        quantity = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingTemplateViews(items: MutableList<IdlingTemplate>): MutableList<IdlingTemplateView> {
    val views = mutableListOf<IdlingTemplateView>()
    for (item in items) {
        views.add(toIdlingTemplateView(item))
    }
    return views
}

fun sumIdlingTemplateView(view: IdlingTemplateView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedQuantity.toDouble() + parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
