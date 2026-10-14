package com.acme.transport.crewing.swap.recorditem

/** How screens and exports show a manual swap score. */
data class ManualSwapScoreView(
    var code: String,
    var titleText: String,
    var checksum: String,
    var discountText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toManualSwapScoreView(item: ManualSwapScore): ManualSwapScoreView {
    return ManualSwapScoreView(
        code = item.code,
        titleText = item.title,
        checksum = item.checksum,
        discountText = "%.2f".format(item.discount),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualSwapScoreViews(items: MutableList<ManualSwapScore>): MutableList<ManualSwapScoreView> {
    val views = mutableListOf<ManualSwapScoreView>()
    for (item in items) {
        views.add(toManualSwapScoreView(item))
    }
    return views
}

fun sumManualSwapScoreView(view: ManualSwapScoreView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
