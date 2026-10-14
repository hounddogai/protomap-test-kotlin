package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire notice. */
data class TireNoticeView(
    var code: String,
    var name: String,
    var pallets: String,
    var segment: String,
    var ratioText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTireNoticeView(item: TireNotice): TireNoticeView {
    return TireNoticeView(
        code = item.code,
        name = truncate(item.name, 120),
        pallets = item.pallets.toString(),
        segment = item.segment,
        ratioText = "%.2f".format(item.ratio),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireNoticeViews(items: MutableList<TireNotice>): MutableList<TireNoticeView> {
    val views = mutableListOf<TireNoticeView>()
    for (item in items) {
        views.add(toTireNoticeView(item))
    }
    return views
}

fun sumTireNoticeView(view: TireNoticeView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedPallets.toDouble() + parsedRatio + parsedAmount
}
