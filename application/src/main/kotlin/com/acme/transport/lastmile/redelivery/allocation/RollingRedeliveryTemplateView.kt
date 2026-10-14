package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling redelivery template. */
data class RollingRedeliveryTemplateView(
    var codeText: String,
    var label: String,
    var lane: String,
    var rate: String,
    var ratio: String,
    var pallets: String,
    var approved: String,
    var statusLabel: String,
)

fun toRollingRedeliveryTemplateView(item: RollingRedeliveryTemplate): RollingRedeliveryTemplateView {
    return RollingRedeliveryTemplateView(
        codeText = item.code,
        label = item.label,
        lane = truncate(item.lane, 64),
        rate = "%.2f".format(item.rate),
        ratio = "%.2f".format(item.ratio),
        pallets = item.pallets.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRedeliveryTemplateViews(items: MutableList<RollingRedeliveryTemplate>): MutableList<RollingRedeliveryTemplateView> {
    val views = mutableListOf<RollingRedeliveryTemplateView>()
    for (item in items) {
        views.add(toRollingRedeliveryTemplateView(item))
    }
    return views
}

fun sumRollingRedeliveryTemplateView(view: RollingRedeliveryTemplateView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRate + parsedRatio + parsedPallets.toDouble()
}
