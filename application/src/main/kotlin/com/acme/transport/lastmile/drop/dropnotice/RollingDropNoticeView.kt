package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling drop notice. */
data class RollingDropNoticeView(
    var code: String,
    var label: String,
    var expiresAtText: String,
    var timezone: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toRollingDropNoticeView(item: RollingDropNotice): RollingDropNoticeView {
    return RollingDropNoticeView(
        code = truncate(item.code, 120),
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        timezone = item.timezone,
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDropNoticeViews(items: MutableList<RollingDropNotice>): MutableList<RollingDropNoticeView> {
    val views = mutableListOf<RollingDropNoticeView>()
    for (item in items) {
        views.add(toRollingDropNoticeView(item))
    }
    return views
}

fun sumRollingDropNoticeView(view: RollingDropNoticeView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedExpiresAt.toDouble() + parsedFloorValue
}
