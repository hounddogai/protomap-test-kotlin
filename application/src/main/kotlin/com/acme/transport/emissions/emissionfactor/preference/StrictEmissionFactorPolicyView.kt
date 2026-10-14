package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict emission factor policy. */
data class StrictEmissionFactorPolicyView(
    var codeText: String,
    var name: String,
    var externalRef: String,
    var trailerIdText: String,
    var routeCode: String,
    var floorValue: String,
    var updatedAtText: String,
    var depot: String,
    var palletsText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toStrictEmissionFactorPolicyView(item: StrictEmissionFactorPolicy): StrictEmissionFactorPolicyView {
    return StrictEmissionFactorPolicyView(
        codeText = item.code,
        name = item.name,
        externalRef = item.externalRef,
        trailerIdText = truncate(item.trailerId, 40),
        routeCode = item.routeCode,
        floorValue = "%.2f".format(item.floorValue),
        updatedAtText = item.updatedAt.toString(),
        depot = item.depot,
        palletsText = item.pallets.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEmissionFactorPolicyViews(items: MutableList<StrictEmissionFactorPolicy>): MutableList<StrictEmissionFactorPolicyView> {
    val views = mutableListOf<StrictEmissionFactorPolicyView>()
    for (item in items) {
        views.add(toStrictEmissionFactorPolicyView(item))
    }
    return views
}

fun sumStrictEmissionFactorPolicyView(view: StrictEmissionFactorPolicyView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedFloorValue + parsedUpdatedAt.toDouble() + parsedPallets.toDouble()
}
