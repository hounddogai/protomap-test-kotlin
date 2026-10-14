package com.acme.transport.fleetops.leasereturn.mapping

/** How screens and exports show a rolling lease return summary. */
data class RollingLeaseReturnSummaryView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var source: String,
    var revision: String,
    var statusLabel: String,
)

fun toRollingLeaseReturnSummaryView(item: RollingLeaseReturnSummary): RollingLeaseReturnSummaryView {
    return RollingLeaseReturnSummaryView(
        codeText = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        source = item.source,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingLeaseReturnSummaryViews(items: MutableList<RollingLeaseReturnSummary>): MutableList<RollingLeaseReturnSummaryView> {
    val views = mutableListOf<RollingLeaseReturnSummaryView>()
    for (item in items) {
        views.add(toRollingLeaseReturnSummaryView(item))
    }
    return views
}

fun sumRollingLeaseReturnSummaryView(view: RollingLeaseReturnSummaryView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedCeiling + parsedRevision.toDouble()
}
