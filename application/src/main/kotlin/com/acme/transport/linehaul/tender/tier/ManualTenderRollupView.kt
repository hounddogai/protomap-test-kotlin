package com.acme.transport.linehaul.tender.tier

/** How screens and exports show a manual tender rollup. */
data class ManualTenderRollupView(
    var codeText: String,
    var titleText: String,
    var revision: String,
    var capacity: String,
    var lane: String,
    var statusLabel: String,
)

fun toManualTenderRollupView(item: ManualTenderRollup): ManualTenderRollupView {
    return ManualTenderRollupView(
        codeText = item.code,
        titleText = item.title,
        revision = item.revision.toString(),
        capacity = item.capacity.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTenderRollupViews(items: MutableList<ManualTenderRollup>): MutableList<ManualTenderRollupView> {
    val views = mutableListOf<ManualTenderRollupView>()
    for (item in items) {
        views.add(toManualTenderRollupView(item))
    }
    return views
}

fun sumManualTenderRollupView(view: ManualTenderRollupView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedRevision.toDouble() + parsedCapacity.toDouble()
}
