package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel draft. */
data class ParcelDraftView(
    var code: String,
    var name: String,
    var amount: String,
    var stopCountText: String,
    var checksum: String,
    var fuelLiters: String,
    var labelText: String,
    var updatedAtText: String,
    var minimum: String,
    var etaMinutes: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toParcelDraftView(item: ParcelDraft): ParcelDraftView {
    return ParcelDraftView(
        code = item.code,
        name = item.name,
        amount = "%.2f".format(item.amount),
        stopCountText = item.stopCount.toString(),
        checksum = item.checksum,
        fuelLiters = "%.2f".format(item.fuelLiters),
        labelText = truncate(item.label, 40),
        updatedAtText = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        etaMinutes = item.etaMinutes.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelDraftViews(items: MutableList<ParcelDraft>): MutableList<ParcelDraftView> {
    val views = mutableListOf<ParcelDraftView>()
    for (item in items) {
        views.add(toParcelDraftView(item))
    }
    return views
}

fun sumParcelDraftView(view: ParcelDraftView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedAmount + parsedStopCount.toDouble() + parsedFuelLiters
}
