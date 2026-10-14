package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default swap checkpoint. */
data class DefaultSwapCheckpointView(
    var code: String,
    var titleText: String,
    var priorityText: String,
    var externalRef: String,
    var minimumText: String,
    var dockText: String,
    var sourceText: String,
    var discount: String,
    var statusLabel: String,
)

fun toDefaultSwapCheckpointView(item: DefaultSwapCheckpoint): DefaultSwapCheckpointView {
    return DefaultSwapCheckpointView(
        code = item.code,
        titleText = truncate(item.title, 120),
        priorityText = item.priority.toString(),
        externalRef = item.externalRef,
        minimumText = "%.2f".format(item.minimum),
        dockText = truncate(item.dock, 64),
        sourceText = item.source,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSwapCheckpointViews(items: MutableList<DefaultSwapCheckpoint>): MutableList<DefaultSwapCheckpointView> {
    val views = mutableListOf<DefaultSwapCheckpointView>()
    for (item in items) {
        views.add(toDefaultSwapCheckpointView(item))
    }
    return views
}

fun sumDefaultSwapCheckpointView(view: DefaultSwapCheckpointView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedPriority.toDouble() + parsedMinimum + parsedDiscount
}
