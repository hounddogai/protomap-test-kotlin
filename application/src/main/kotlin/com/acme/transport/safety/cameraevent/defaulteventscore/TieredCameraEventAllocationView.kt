package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered camera event allocation. */
data class TieredCameraEventAllocationView(
    var code: String,
    var titleText: String,
    var sequence: String,
    var contactEmailText: String,
    var budget: String,
    var statusLabel: String,
)

fun toTieredCameraEventAllocationView(item: TieredCameraEventAllocation): TieredCameraEventAllocationView {
    return TieredCameraEventAllocationView(
        code = item.code,
        titleText = truncate(item.title, 120),
        sequence = item.sequence.toString(),
        contactEmailText = item.contactEmail,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCameraEventAllocationViews(items: MutableList<TieredCameraEventAllocation>): MutableList<TieredCameraEventAllocationView> {
    val views = mutableListOf<TieredCameraEventAllocationView>()
    for (item in items) {
        views.add(toTieredCameraEventAllocationView(item))
    }
    return views
}

fun sumTieredCameraEventAllocationView(view: TieredCameraEventAllocationView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedSequence.toDouble() + parsedBudget
}
