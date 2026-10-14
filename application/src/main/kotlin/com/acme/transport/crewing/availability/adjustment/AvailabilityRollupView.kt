package com.acme.transport.crewing.availability.adjustment

/** How screens and exports show a availability rollup. */
data class AvailabilityRollupView(
    var code: String,
    var label: String,
    var endsAt: String,
    var quantity: String,
    var threshold: String,
    var contactEmail: String,
    var lane: String,
    var statusLabel: String,
)

fun toAvailabilityRollupView(item: AvailabilityRollup): AvailabilityRollupView {
    return AvailabilityRollupView(
        code = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        threshold = "%.2f".format(item.threshold),
        contactEmail = item.contactEmail,
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityRollupViews(items: MutableList<AvailabilityRollup>): MutableList<AvailabilityRollupView> {
    val views = mutableListOf<AvailabilityRollupView>()
    for (item in items) {
        views.add(toAvailabilityRollupView(item))
    }
    return views
}

fun sumAvailabilityRollupView(view: AvailabilityRollupView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedEndsAt.toDouble() + parsedQuantity.toDouble() + parsedThreshold
}
