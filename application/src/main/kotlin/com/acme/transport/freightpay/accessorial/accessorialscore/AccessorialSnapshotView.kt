package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial snapshot. */
data class AccessorialSnapshotView(
    var code: String,
    var title: String,
    var visible: String,
    var unitText: String,
    var durationMinutesText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toAccessorialSnapshotView(item: AccessorialSnapshot): AccessorialSnapshotView {
    return AccessorialSnapshotView(
        code = truncate(item.code, 120),
        title = item.title,
        visible = "${item.visible}",
        unitText = item.unit,
        durationMinutesText = item.durationMinutes.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialSnapshotViews(items: MutableList<AccessorialSnapshot>): MutableList<AccessorialSnapshotView> {
    val views = mutableListOf<AccessorialSnapshotView>()
    for (item in items) {
        views.add(toAccessorialSnapshotView(item))
    }
    return views
}

fun sumAccessorialSnapshotView(view: AccessorialSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedCreatedAt.toDouble()
}
