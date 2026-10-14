package com.acme.transport.safety.cameraevent.profile

/** How screens and exports show a scheduled camera event milestone. */
data class ScheduledCameraEventMilestoneView(
    var codeText: String,
    var name: String,
    var weight: String,
    var quantityText: String,
    var shiftCode: String,
    var score: String,
    var statusLabel: String,
)

fun toScheduledCameraEventMilestoneView(item: ScheduledCameraEventMilestone): ScheduledCameraEventMilestoneView {
    return ScheduledCameraEventMilestoneView(
        codeText = item.code,
        name = item.name,
        weight = "%.2f".format(item.weight),
        quantityText = item.quantity.toString(),
        shiftCode = item.shiftCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCameraEventMilestoneViews(items: MutableList<ScheduledCameraEventMilestone>): MutableList<ScheduledCameraEventMilestoneView> {
    val views = mutableListOf<ScheduledCameraEventMilestoneView>()
    for (item in items) {
        views.add(toScheduledCameraEventMilestoneView(item))
    }
    return views
}

fun sumScheduledCameraEventMilestoneView(view: ScheduledCameraEventMilestoneView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedWeight + parsedQuantity.toDouble() + parsedScore
}
