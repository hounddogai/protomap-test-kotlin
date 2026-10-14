package com.acme.transport.crewing.roster.cycle

/** How screens and exports show a roster adjustment. */
data class RosterAdjustmentView(
    var codeText: String,
    var name: String,
    var etaMinutes: String,
    var revision: String,
    var ceiling: String,
    var batchSize: String,
    var sequence: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toRosterAdjustmentView(item: RosterAdjustment): RosterAdjustmentView {
    return RosterAdjustmentView(
        codeText = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        batchSize = item.batchSize.toString(),
        sequence = item.sequence.toString(),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterAdjustmentViews(items: MutableList<RosterAdjustment>): MutableList<RosterAdjustmentView> {
    val views = mutableListOf<RosterAdjustmentView>()
    for (item in items) {
        views.add(toRosterAdjustmentView(item))
    }
    return views
}

fun sumRosterAdjustmentView(view: RosterAdjustmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble() + parsedCeiling
}
