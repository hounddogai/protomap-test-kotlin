package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling drop milestone. */
data class RollingDropMilestoneView(
    var codeText: String,
    var label: String,
    var ratio: String,
    var ceiling: String,
    var name: String,
    var locale: String,
    var title: String,
    var version: String,
    var shiftCode: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toRollingDropMilestoneView(item: RollingDropMilestone): RollingDropMilestoneView {
    return RollingDropMilestoneView(
        codeText = item.code,
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        ceiling = "%.2f".format(item.ceiling),
        name = item.name,
        locale = item.locale,
        title = item.title,
        version = item.version.toString(),
        shiftCode = truncate(item.shiftCode, 64),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDropMilestoneViews(items: MutableList<RollingDropMilestone>): MutableList<RollingDropMilestoneView> {
    val views = mutableListOf<RollingDropMilestoneView>()
    for (item in items) {
        views.add(toRollingDropMilestoneView(item))
    }
    return views
}

fun sumRollingDropMilestoneView(view: RollingDropMilestoneView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedRatio + parsedCeiling + parsedVersion.toDouble()
}
