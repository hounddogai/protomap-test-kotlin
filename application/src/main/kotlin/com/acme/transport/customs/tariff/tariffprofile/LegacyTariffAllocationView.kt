package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy tariff allocation. */
data class LegacyTariffAllocationView(
    var codeText: String,
    var name: String,
    var unit: String,
    var expiresAt: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toLegacyTariffAllocationView(item: LegacyTariffAllocation): LegacyTariffAllocationView {
    return LegacyTariffAllocationView(
        codeText = item.code,
        name = truncate(item.name, 40),
        unit = item.unit,
        expiresAt = item.expiresAt.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTariffAllocationViews(items: MutableList<LegacyTariffAllocation>): MutableList<LegacyTariffAllocationView> {
    val views = mutableListOf<LegacyTariffAllocationView>()
    for (item in items) {
        views.add(toLegacyTariffAllocationView(item))
    }
    return views
}

fun sumLegacyTariffAllocationView(view: LegacyTariffAllocationView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
