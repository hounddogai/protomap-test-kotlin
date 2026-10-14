package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tariff draft. */
data class StrictTariffDraftView(
    var code: String,
    var title: String,
    var description: String,
    var fuelLiters: String,
    var palletsText: String,
    var revisionText: String,
    var driverId: String,
    var region: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toStrictTariffDraftView(item: StrictTariffDraft): StrictTariffDraftView {
    return StrictTariffDraftView(
        code = item.code,
        title = item.title,
        description = truncate(item.description, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        palletsText = item.pallets.toString(),
        revisionText = item.revision.toString(),
        driverId = item.driverId,
        region = truncate(item.region, 40),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTariffDraftViews(items: MutableList<StrictTariffDraft>): MutableList<StrictTariffDraftView> {
    val views = mutableListOf<StrictTariffDraftView>()
    for (item in items) {
        views.add(toStrictTariffDraftView(item))
    }
    return views
}

fun sumStrictTariffDraftView(view: StrictTariffDraftView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedFuelLiters + parsedPallets.toDouble() + parsedRevision.toDouble()
}
