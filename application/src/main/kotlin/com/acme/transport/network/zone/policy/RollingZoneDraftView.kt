package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling zone draft. */
data class RollingZoneDraftView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var name: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toRollingZoneDraftView(item: RollingZoneDraft): RollingZoneDraftView {
    return RollingZoneDraftView(
        code = truncate(item.code, 120),
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingZoneDraftViews(items: MutableList<RollingZoneDraft>): MutableList<RollingZoneDraftView> {
    val views = mutableListOf<RollingZoneDraftView>()
    for (item in items) {
        views.add(toRollingZoneDraftView(item))
    }
    return views
}

fun sumRollingZoneDraftView(view: RollingZoneDraftView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedExpiresAt.toDouble()
}
