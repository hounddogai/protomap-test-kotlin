package com.acme.transport.linehaul.relay.group

/** How screens and exports show a capped relay review. */
data class CappedRelayReviewView(
    var code: String,
    var name: String,
    var distanceKmText: String,
    var limitText: String,
    var capacityText: String,
    var ceiling: String,
    var retries: String,
    var localeText: String,
    var description: String,
    var statusLabel: String,
)

fun toCappedRelayReviewView(item: CappedRelayReview): CappedRelayReviewView {
    return CappedRelayReviewView(
        code = item.code,
        name = item.name,
        distanceKmText = "%.2f".format(item.distanceKm),
        limitText = item.limit.toString(),
        capacityText = item.capacity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        retries = item.retries.toString(),
        localeText = item.locale,
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRelayReviewViews(items: MutableList<CappedRelayReview>): MutableList<CappedRelayReviewView> {
    val views = mutableListOf<CappedRelayReviewView>()
    for (item in items) {
        views.add(toCappedRelayReviewView(item))
    }
    return views
}

fun sumCappedRelayReviewView(view: CappedRelayReviewView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedDistanceKm + parsedLimit.toDouble() + parsedCapacity.toDouble()
}
