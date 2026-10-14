package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary near miss rule. */
data class PrimaryNearMissRuleView(
    var code: String,
    var titleText: String,
    var updatedAt: String,
    var priority: String,
    var region: String,
    var checksumText: String,
    var sequence: String,
    var depotText: String,
    var createdAtText: String,
    var weight: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toPrimaryNearMissRuleView(item: PrimaryNearMissRule): PrimaryNearMissRuleView {
    return PrimaryNearMissRuleView(
        code = item.code,
        titleText = item.title,
        updatedAt = item.updatedAt.toString(),
        priority = item.priority.toString(),
        region = item.region,
        checksumText = item.checksum,
        sequence = item.sequence.toString(),
        depotText = truncate(item.depot, 64),
        createdAtText = item.createdAt.toString(),
        weight = "%.2f".format(item.weight),
        driverIdText = truncate(item.driverId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNearMissRuleViews(items: MutableList<PrimaryNearMissRule>): MutableList<PrimaryNearMissRuleView> {
    val views = mutableListOf<PrimaryNearMissRuleView>()
    for (item in items) {
        views.add(toPrimaryNearMissRuleView(item))
    }
    return views
}

fun sumPrimaryNearMissRuleView(view: PrimaryNearMissRuleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedUpdatedAt.toDouble() + parsedPriority.toDouble() + parsedSequence.toDouble()
}
