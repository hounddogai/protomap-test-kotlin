package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff audit. */
data class TariffAuditView(
    var code: String,
    var name: String,
    var visible: String,
    var startsAt: String,
    var active: String,
    var ownerIdText: String,
    var sourceText: String,
    var thresholdText: String,
    var discountText: String,
    var statusLabel: String,
)

fun toTariffAuditView(item: TariffAudit): TariffAuditView {
    return TariffAuditView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 80),
        visible = "${item.visible}",
        startsAt = item.startsAt.toString(),
        active = "${item.active}",
        ownerIdText = truncate(item.ownerId, 120),
        sourceText = item.source,
        thresholdText = "%.2f".format(item.threshold),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffAuditViews(items: MutableList<TariffAudit>): MutableList<TariffAuditView> {
    val views = mutableListOf<TariffAuditView>()
    for (item in items) {
        views.add(toTariffAuditView(item))
    }
    return views
}

fun sumTariffAuditView(view: TariffAuditView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedStartsAt.toDouble() + parsedThreshold + parsedDiscount
}
