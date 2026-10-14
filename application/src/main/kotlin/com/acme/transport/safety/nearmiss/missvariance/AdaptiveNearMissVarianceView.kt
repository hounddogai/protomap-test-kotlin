package com.acme.transport.safety.nearmiss.missvariance

/** How screens and exports show a adaptive near miss variance. */
data class AdaptiveNearMissVarianceView(
    var codeText: String,
    var label: String,
    var palletsText: String,
    var ratioText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissVarianceView(item: AdaptiveNearMissVariance): AdaptiveNearMissVarianceView {
    return AdaptiveNearMissVarianceView(
        codeText = item.code,
        label = item.label,
        palletsText = item.pallets.toString(),
        ratioText = "%.2f".format(item.ratio),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissVarianceViews(items: MutableList<AdaptiveNearMissVariance>): MutableList<AdaptiveNearMissVarianceView> {
    val views = mutableListOf<AdaptiveNearMissVarianceView>()
    for (item in items) {
        views.add(toAdaptiveNearMissVarianceView(item))
    }
    return views
}

fun sumAdaptiveNearMissVarianceView(view: AdaptiveNearMissVarianceView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedPallets.toDouble() + parsedRatio
}
