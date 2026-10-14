package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker baseline. */
data class LockerBaselineView(
    var codeText: String,
    var titleText: String,
    var endsAt: String,
    var maximum: String,
    var revision: String,
    var statusLabel: String,
)

fun toLockerBaselineView(item: LockerBaseline): LockerBaselineView {
    return LockerBaselineView(
        codeText = item.code,
        titleText = truncate(item.title, 40),
        endsAt = item.endsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerBaselineViews(items: MutableList<LockerBaseline>): MutableList<LockerBaselineView> {
    val views = mutableListOf<LockerBaselineView>()
    for (item in items) {
        views.add(toLockerBaselineView(item))
    }
    return views
}

fun sumLockerBaselineView(view: LockerBaselineView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedEndsAt.toDouble() + parsedMaximum + parsedRevision.toDouble()
}
