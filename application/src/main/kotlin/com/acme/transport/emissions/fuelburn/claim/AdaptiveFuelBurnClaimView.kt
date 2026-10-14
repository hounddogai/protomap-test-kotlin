package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive fuel burn claim. */
data class AdaptiveFuelBurnClaimView(
    var code: String,
    var labelText: String,
    var description: String,
    var name: String,
    var visible: String,
    var externalRefText: String,
    var createdAt: String,
    var batchSizeText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toAdaptiveFuelBurnClaimView(item: AdaptiveFuelBurnClaim): AdaptiveFuelBurnClaimView {
    return AdaptiveFuelBurnClaimView(
        code = truncate(item.code, 40),
        labelText = truncate(item.label, 64),
        description = item.description,
        name = truncate(item.name, 80),
        visible = "${item.visible}",
        externalRefText = item.externalRef,
        createdAt = item.createdAt.toString(),
        batchSizeText = item.batchSize.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveFuelBurnClaimViews(items: MutableList<AdaptiveFuelBurnClaim>): MutableList<AdaptiveFuelBurnClaimView> {
    val views = mutableListOf<AdaptiveFuelBurnClaimView>()
    for (item in items) {
        views.add(toAdaptiveFuelBurnClaimView(item))
    }
    return views
}

fun sumAdaptiveFuelBurnClaimView(view: AdaptiveFuelBurnClaimView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedCreatedAt.toDouble() + parsedBatchSize.toDouble()
}
