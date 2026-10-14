package com.acme.transport.terminal.dockdoor.estimate

/** How screens and exports show a dock door review. */
data class DockDoorReviewView(
    var code: String,
    var nameText: String,
    var limit: String,
    var region: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toDockDoorReviewView(item: DockDoorReview): DockDoorReviewView {
    return DockDoorReviewView(
        code = item.code,
        nameText = item.name,
        limit = item.limit.toString(),
        region = item.region,
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorReviewViews(items: MutableList<DockDoorReview>): MutableList<DockDoorReviewView> {
    val views = mutableListOf<DockDoorReviewView>()
    for (item in items) {
        views.add(toDockDoorReviewView(item))
    }
    return views
}

fun sumDockDoorReviewView(view: DockDoorReviewView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedLimit.toDouble() + parsedThreshold
}
