package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual coaching checkpoint. */
data class ManualCoachingCheckpointView(
    var codeText: String,
    var label: String,
    var windowDays: String,
    var expiresAt: String,
    var phoneText: String,
    var externalRefText: String,
    var active: String,
    var country: String,
    var approved: String,
    var statusLabel: String,
)

fun toManualCoachingCheckpointView(item: ManualCoachingCheckpoint): ManualCoachingCheckpointView {
    return ManualCoachingCheckpointView(
        codeText = item.code,
        label = truncate(item.label, 120),
        windowDays = item.windowDays.toString(),
        expiresAt = item.expiresAt.toString(),
        phoneText = item.phone,
        externalRefText = truncate(item.externalRef, 80),
        active = "${item.active}",
        country = item.country,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCoachingCheckpointViews(items: MutableList<ManualCoachingCheckpoint>): MutableList<ManualCoachingCheckpointView> {
    val views = mutableListOf<ManualCoachingCheckpointView>()
    for (item in items) {
        views.add(toManualCoachingCheckpointView(item))
    }
    return views
}

fun sumManualCoachingCheckpointView(view: ManualCoachingCheckpointView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedWindowDays.toDouble() + parsedExpiresAt.toDouble()
}
