package com.acme.transport.linehaul.trailer.threshold

/** How screens and exports show a trailer allocation. */
data class TrailerAllocationView(
    var code: String,
    var name: String,
    var threshold: String,
    var channel: String,
    var batchSize: String,
    var ownerIdText: String,
    var endsAt: String,
    var updatedAt: String,
    var country: String,
    var active: String,
    var statusLabel: String,
)

fun toTrailerAllocationView(item: TrailerAllocation): TrailerAllocationView {
    return TrailerAllocationView(
        code = item.code,
        name = item.name,
        threshold = "%.2f".format(item.threshold),
        channel = item.channel,
        batchSize = item.batchSize.toString(),
        ownerIdText = item.ownerId,
        endsAt = item.endsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        country = item.country,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerAllocationViews(items: MutableList<TrailerAllocation>): MutableList<TrailerAllocationView> {
    val views = mutableListOf<TrailerAllocationView>()
    for (item in items) {
        views.add(toTrailerAllocationView(item))
    }
    return views
}

fun sumTrailerAllocationView(view: TrailerAllocationView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedThreshold + parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}
