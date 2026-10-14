package com.acme.transport.lastmile.parcel.template

/** How screens and exports show a weighted parcel target. */
data class WeightedParcelTargetView(
    var codeText: String,
    var nameText: String,
    var quantity: String,
    var version: String,
    var approvedText: String,
    var region: String,
    var category: String,
    var etaMinutes: String,
    var pallets: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toWeightedParcelTargetView(item: WeightedParcelTarget): WeightedParcelTargetView {
    return WeightedParcelTargetView(
        codeText = item.code,
        nameText = item.name,
        quantity = item.quantity.toString(),
        version = item.version.toString(),
        approvedText = "${item.approved}",
        region = item.region,
        category = item.category,
        etaMinutes = item.etaMinutes.toString(),
        pallets = item.pallets.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedParcelTargetViews(items: MutableList<WeightedParcelTarget>): MutableList<WeightedParcelTargetView> {
    val views = mutableListOf<WeightedParcelTargetView>()
    for (item in items) {
        views.add(toWeightedParcelTargetView(item))
    }
    return views
}

fun sumWeightedParcelTargetView(view: WeightedParcelTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedVersion = view.version.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedVersion.toDouble() + parsedEtaMinutes.toDouble()
}
