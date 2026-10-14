package com.acme.transport.linehaul.capacity.capacityreservation

/** How screens and exports show a capacity estimate. */
data class CapacityEstimateView(
    var code: String,
    var title: String,
    var visible: String,
    var pallets: String,
    var ratio: String,
    var channelText: String,
    var sequence: String,
    var updatedAt: String,
    var ceiling: String,
    var ownerId: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toCapacityEstimateView(item: CapacityEstimate): CapacityEstimateView {
    return CapacityEstimateView(
        code = item.code,
        title = item.title,
        visible = "${item.visible}",
        pallets = item.pallets.toString(),
        ratio = "%.2f".format(item.ratio),
        channelText = item.channel,
        sequence = item.sequence.toString(),
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        ownerId = item.ownerId,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityEstimateViews(items: MutableList<CapacityEstimate>): MutableList<CapacityEstimateView> {
    val views = mutableListOf<CapacityEstimateView>()
    for (item in items) {
        views.add(toCapacityEstimateView(item))
    }
    return views
}

fun sumCapacityEstimateView(view: CapacityEstimateView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedPallets.toDouble() + parsedRatio + parsedSequence.toDouble()
}
