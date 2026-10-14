package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional capacity request. */
data class RegionalCapacityRequestView(
    var code: String,
    var name: String,
    var amount: String,
    var versionText: String,
    var checksum: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toRegionalCapacityRequestView(item: RegionalCapacityRequest): RegionalCapacityRequestView {
    return RegionalCapacityRequestView(
        code = item.code,
        name = truncate(item.name, 64),
        amount = "%.2f".format(item.amount),
        versionText = item.version.toString(),
        checksum = item.checksum,
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityRequestViews(items: MutableList<RegionalCapacityRequest>): MutableList<RegionalCapacityRequestView> {
    val views = mutableListOf<RegionalCapacityRequestView>()
    for (item in items) {
        views.add(toRegionalCapacityRequestView(item))
    }
    return views
}

fun sumRegionalCapacityRequestView(view: RegionalCapacityRequestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedAmount + parsedVersion.toDouble() + parsedCapacity.toDouble()
}
