package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender bundle. */
data class TenderBundleView(
    var code: String,
    var title: String,
    var source: String,
    var driverId: String,
    var graceDays: String,
    var name: String,
    var statusLabel: String,
)

fun toTenderBundleView(item: TenderBundle): TenderBundleView {
    return TenderBundleView(
        code = truncate(item.code, 120),
        title = item.title,
        source = truncate(item.source, 64),
        driverId = item.driverId,
        graceDays = item.graceDays.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderBundleViews(items: MutableList<TenderBundle>): MutableList<TenderBundleView> {
    val views = mutableListOf<TenderBundleView>()
    for (item in items) {
        views.add(toTenderBundleView(item))
    }
    return views
}

fun sumTenderBundleView(view: TenderBundleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
