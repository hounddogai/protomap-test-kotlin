package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss summary. */
data class NearMissSummaryView(
    var code: String,
    var name: String,
    var cost: String,
    var limit: String,
    var revision: String,
    var version: String,
    var visible: String,
    var statusLabel: String,
)

fun toNearMissSummaryView(item: NearMissSummary): NearMissSummaryView {
    return NearMissSummaryView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        cost = "%.2f".format(item.cost),
        limit = item.limit.toString(),
        revision = item.revision.toString(),
        version = item.version.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissSummaryViews(items: MutableList<NearMissSummary>): MutableList<NearMissSummaryView> {
    val views = mutableListOf<NearMissSummaryView>()
    for (item in items) {
        views.add(toNearMissSummaryView(item))
    }
    return views
}

fun sumNearMissSummaryView(view: NearMissSummaryView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedCost + parsedLimit.toDouble() + parsedRevision.toDouble()
}
