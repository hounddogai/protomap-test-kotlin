package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone template. */
data class ZoneTemplateView(
    var code: String,
    var name: String,
    var country: String,
    var externalRef: String,
    var source: String,
    var shiftCode: String,
    var priorityText: String,
    var dock: String,
    var driverId: String,
    var channelText: String,
    var statusLabel: String,
)

fun toZoneTemplateView(item: ZoneTemplate): ZoneTemplateView {
    return ZoneTemplateView(
        code = item.code,
        name = item.name,
        country = truncate(item.country, 64),
        externalRef = item.externalRef,
        source = item.source,
        shiftCode = item.shiftCode,
        priorityText = item.priority.toString(),
        dock = truncate(item.dock, 40),
        driverId = item.driverId,
        channelText = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneTemplateViews(items: MutableList<ZoneTemplate>): MutableList<ZoneTemplateView> {
    val views = mutableListOf<ZoneTemplateView>()
    for (item in items) {
        views.add(toZoneTemplateView(item))
    }
    return views
}

fun sumZoneTemplateView(view: ZoneTemplateView): Double {
    val parsedPriority = view.priorityText.toInt()
    return parsedPriority.toDouble()
}
