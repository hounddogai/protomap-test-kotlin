package com.acme.transport.terminal.dwell.exception

/** How screens and exports show a pending dwell rollup. */
data class PendingDwellRollupView(
    var code: String,
    var titleText: String,
    var notesText: String,
    var region: String,
    var shiftCode: String,
    var limit: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toPendingDwellRollupView(item: PendingDwellRollup): PendingDwellRollupView {
    return PendingDwellRollupView(
        code = item.code,
        titleText = item.title,
        notesText = item.notes,
        region = item.region,
        shiftCode = item.shiftCode,
        limit = item.limit.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDwellRollupViews(items: MutableList<PendingDwellRollup>): MutableList<PendingDwellRollupView> {
    val views = mutableListOf<PendingDwellRollupView>()
    for (item in items) {
        views.add(toPendingDwellRollupView(item))
    }
    return views
}

fun sumPendingDwellRollupView(view: PendingDwellRollupView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}
