package com.acme.transport.lastmile.parcel.parcelreservation

/** How screens and exports show a weighted parcel override. */
data class WeightedParcelOverrideView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var palletsText: String,
    var visibleText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toWeightedParcelOverrideView(item: WeightedParcelOverride): WeightedParcelOverrideView {
    return WeightedParcelOverrideView(
        code = item.code,
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        palletsText = item.pallets.toString(),
        visibleText = "${item.visible}",
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedParcelOverrideViews(items: MutableList<WeightedParcelOverride>): MutableList<WeightedParcelOverrideView> {
    val views = mutableListOf<WeightedParcelOverrideView>()
    for (item in items) {
        views.add(toWeightedParcelOverrideView(item))
    }
    return views
}

fun sumWeightedParcelOverrideView(view: WeightedParcelOverrideView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedBatchSize.toDouble() + parsedPallets.toDouble() + parsedAttempts.toDouble()
}
