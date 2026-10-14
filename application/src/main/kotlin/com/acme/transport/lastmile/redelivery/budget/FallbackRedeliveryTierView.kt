package com.acme.transport.lastmile.redelivery.budget

/** How screens and exports show a fallback redelivery tier. */
data class FallbackRedeliveryTierView(
    var codeText: String,
    var label: String,
    var maximum: String,
    var depot: String,
    var limit: String,
    var sequence: String,
    var region: String,
    var driverId: String,
    var versionText: String,
    var statusLabel: String,
)

fun toFallbackRedeliveryTierView(item: FallbackRedeliveryTier): FallbackRedeliveryTierView {
    return FallbackRedeliveryTierView(
        codeText = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        depot = item.depot,
        limit = item.limit.toString(),
        sequence = item.sequence.toString(),
        region = item.region,
        driverId = item.driverId,
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRedeliveryTierViews(items: MutableList<FallbackRedeliveryTier>): MutableList<FallbackRedeliveryTierView> {
    val views = mutableListOf<FallbackRedeliveryTierView>()
    for (item in items) {
        views.add(toFallbackRedeliveryTierView(item))
    }
    return views
}

fun sumFallbackRedeliveryTierView(view: FallbackRedeliveryTierView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedMaximum + parsedLimit.toDouble() + parsedSequence.toDouble()
}
