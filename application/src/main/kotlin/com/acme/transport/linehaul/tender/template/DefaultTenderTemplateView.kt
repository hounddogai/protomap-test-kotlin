package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tender template. */
data class DefaultTenderTemplateView(
    var code: String,
    var name: String,
    var carrier: String,
    var etaMinutes: String,
    var lane: String,
    var pinned: String,
    var minimum: String,
    var maximum: String,
    var statusLabel: String,
)

fun toDefaultTenderTemplateView(item: DefaultTenderTemplate): DefaultTenderTemplateView {
    return DefaultTenderTemplateView(
        code = item.code,
        name = item.name,
        carrier = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        lane = truncate(item.lane, 80),
        pinned = "${item.pinned}",
        minimum = "%.2f".format(item.minimum),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTenderTemplateViews(items: MutableList<DefaultTenderTemplate>): MutableList<DefaultTenderTemplateView> {
    val views = mutableListOf<DefaultTenderTemplateView>()
    for (item in items) {
        views.add(toDefaultTenderTemplateView(item))
    }
    return views
}

fun sumDefaultTenderTemplateView(view: DefaultTenderTemplateView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMinimum + parsedMaximum
}
