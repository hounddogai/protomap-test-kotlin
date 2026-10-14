package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone template. */
data class GlobalZoneTemplateView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var loadKgText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toGlobalZoneTemplateView(item: GlobalZoneTemplate): GlobalZoneTemplateView {
    return GlobalZoneTemplateView(
        code = item.code,
        title = item.title,
        shiftCode = item.shiftCode,
        loadKgText = "%.2f".format(item.loadKg),
        checksumText = truncate(item.checksum, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneTemplateViews(items: MutableList<GlobalZoneTemplate>): MutableList<GlobalZoneTemplateView> {
    val views = mutableListOf<GlobalZoneTemplateView>()
    for (item in items) {
        views.add(toGlobalZoneTemplateView(item))
    }
    return views
}

fun sumGlobalZoneTemplateView(view: GlobalZoneTemplateView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
