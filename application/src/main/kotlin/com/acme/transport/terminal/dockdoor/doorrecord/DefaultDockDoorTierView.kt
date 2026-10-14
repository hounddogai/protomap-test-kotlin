package com.acme.transport.terminal.dockdoor.doorrecord

/** How screens and exports show a default dock door tier. */
data class DefaultDockDoorTierView(
    var code: String,
    var name: String,
    var sequenceText: String,
    var graceDaysText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toDefaultDockDoorTierView(item: DefaultDockDoorTier): DefaultDockDoorTierView {
    return DefaultDockDoorTierView(
        code = item.code,
        name = item.name,
        sequenceText = item.sequence.toString(),
        graceDaysText = item.graceDays.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDockDoorTierViews(items: MutableList<DefaultDockDoorTier>): MutableList<DefaultDockDoorTierView> {
    val views = mutableListOf<DefaultDockDoorTierView>()
    for (item in items) {
        views.add(toDefaultDockDoorTierView(item))
    }
    return views
}

fun sumDefaultDockDoorTierView(view: DefaultDockDoorTierView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedSequence.toDouble() + parsedGraceDays.toDouble() + parsedCeiling
}
