package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tariff policy. */
data class LenientTariffPolicyView(
    var code: String,
    var label: String,
    var source: String,
    var pallets: String,
    var phone: String,
    var approved: String,
    var capacityText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toLenientTariffPolicyView(item: LenientTariffPolicy): LenientTariffPolicyView {
    return LenientTariffPolicyView(
        code = item.code,
        label = item.label,
        source = truncate(item.source, 80),
        pallets = item.pallets.toString(),
        phone = item.phone,
        approved = "${item.approved}",
        capacityText = item.capacity.toString(),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTariffPolicyViews(items: MutableList<LenientTariffPolicy>): MutableList<LenientTariffPolicyView> {
    val views = mutableListOf<LenientTariffPolicyView>()
    for (item in items) {
        views.add(toLenientTariffPolicyView(item))
    }
    return views
}

fun sumLenientTariffPolicyView(view: LenientTariffPolicyView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedPallets.toDouble() + parsedCapacity.toDouble() + parsedLimit.toDouble()
}
