package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual idling target. */
data class ManualIdlingTargetView(
    var code: String,
    var title: String,
    var reviewer: String,
    var visibleText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toManualIdlingTargetView(item: ManualIdlingTarget): ManualIdlingTargetView {
    return ManualIdlingTargetView(
        code = truncate(item.code, 80),
        title = item.title,
        reviewer = item.reviewer,
        visibleText = "${item.visible}",
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualIdlingTargetViews(items: MutableList<ManualIdlingTarget>): MutableList<ManualIdlingTargetView> {
    val views = mutableListOf<ManualIdlingTargetView>()
    for (item in items) {
        views.add(toManualIdlingTargetView(item))
    }
    return views
}

fun sumManualIdlingTargetView(view: ManualIdlingTargetView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
