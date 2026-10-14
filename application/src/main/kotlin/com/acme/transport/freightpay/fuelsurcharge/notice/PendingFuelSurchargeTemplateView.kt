package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel surcharge template. */
data class PendingFuelSurchargeTemplateView(
    var code: String,
    var name: String,
    var startsAt: String,
    var locale: String,
    var pinned: String,
    var ratio: String,
    var minimum: String,
    var visibleText: String,
    var version: String,
    var revision: String,
    var statusLabel: String,
)

fun toPendingFuelSurchargeTemplateView(item: PendingFuelSurchargeTemplate): PendingFuelSurchargeTemplateView {
    return PendingFuelSurchargeTemplateView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 40),
        startsAt = item.startsAt.toString(),
        locale = item.locale,
        pinned = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        minimum = "%.2f".format(item.minimum),
        visibleText = "${item.visible}",
        version = item.version.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelSurchargeTemplateViews(items: MutableList<PendingFuelSurchargeTemplate>): MutableList<PendingFuelSurchargeTemplateView> {
    val views = mutableListOf<PendingFuelSurchargeTemplateView>()
    for (item in items) {
        views.add(toPendingFuelSurchargeTemplateView(item))
    }
    return views
}

fun sumPendingFuelSurchargeTemplateView(view: PendingFuelSurchargeTemplateView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedRatio + parsedMinimum
}
