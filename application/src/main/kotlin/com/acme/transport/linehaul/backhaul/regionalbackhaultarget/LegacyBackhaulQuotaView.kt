package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy backhaul quota. */
data class LegacyBackhaulQuotaView(
    var codeText: String,
    var title: String,
    var cost: String,
    var label: String,
    var unit: String,
    var phone: String,
    var statusLabel: String,
)

fun toLegacyBackhaulQuotaView(item: LegacyBackhaulQuota): LegacyBackhaulQuotaView {
    return LegacyBackhaulQuotaView(
        codeText = item.code,
        title = item.title,
        cost = "%.2f".format(item.cost),
        label = item.label,
        unit = truncate(item.unit, 80),
        phone = truncate(item.phone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBackhaulQuotaViews(items: MutableList<LegacyBackhaulQuota>): MutableList<LegacyBackhaulQuotaView> {
    val views = mutableListOf<LegacyBackhaulQuotaView>()
    for (item in items) {
        views.add(toLegacyBackhaulQuotaView(item))
    }
    return views
}

fun sumLegacyBackhaulQuotaView(view: LegacyBackhaulQuotaView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
