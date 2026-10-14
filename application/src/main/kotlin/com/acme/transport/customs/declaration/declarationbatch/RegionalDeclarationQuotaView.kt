package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional declaration quota. */
data class RegionalDeclarationQuotaView(
    var code: String,
    var label: String,
    var segment: String,
    var marginText: String,
    var name: String,
    var fuelLiters: String,
    var laneText: String,
    var country: String,
    var updatedAtText: String,
    var ratio: String,
    var notes: String,
    var statusLabel: String,
)

fun toRegionalDeclarationQuotaView(item: RegionalDeclarationQuota): RegionalDeclarationQuotaView {
    return RegionalDeclarationQuotaView(
        code = item.code,
        label = item.label,
        segment = item.segment,
        marginText = "%.2f".format(item.margin),
        name = truncate(item.name, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        laneText = item.lane,
        country = item.country,
        updatedAtText = item.updatedAt.toString(),
        ratio = "%.2f".format(item.ratio),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDeclarationQuotaViews(items: MutableList<RegionalDeclarationQuota>): MutableList<RegionalDeclarationQuotaView> {
    val views = mutableListOf<RegionalDeclarationQuotaView>()
    for (item in items) {
        views.add(toRegionalDeclarationQuotaView(item))
    }
    return views
}

fun sumRegionalDeclarationQuotaView(view: RegionalDeclarationQuotaView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedMargin + parsedFuelLiters + parsedUpdatedAt.toDouble()
}
