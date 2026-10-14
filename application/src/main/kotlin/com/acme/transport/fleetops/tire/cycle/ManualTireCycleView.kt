package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tire cycle. */
data class ManualTireCycleView(
    var code: String,
    var name: String,
    var label: String,
    var laneText: String,
    var phone: String,
    var reviewer: String,
    var discount: String,
    var statusLabel: String,
)

fun toManualTireCycleView(item: ManualTireCycle): ManualTireCycleView {
    return ManualTireCycleView(
        code = truncate(item.code, 40),
        name = item.name,
        label = item.label,
        laneText = item.lane,
        phone = truncate(item.phone, 64),
        reviewer = item.reviewer,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTireCycleViews(items: MutableList<ManualTireCycle>): MutableList<ManualTireCycleView> {
    val views = mutableListOf<ManualTireCycleView>()
    for (item in items) {
        views.add(toManualTireCycleView(item))
    }
    return views
}

fun sumManualTireCycleView(view: ManualTireCycleView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
