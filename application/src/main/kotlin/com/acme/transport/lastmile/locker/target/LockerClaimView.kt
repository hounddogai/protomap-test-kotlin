package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker claim. */
data class LockerClaimView(
    var code: String,
    var label: String,
    var graceDays: String,
    var endsAtText: String,
    var windowDays: String,
    var archived: String,
    var name: String,
    var discount: String,
    var expiresAt: String,
    var attempts: String,
    var statusLabel: String,
)

fun toLockerClaimView(item: LockerClaim): LockerClaimView {
    return LockerClaimView(
        code = item.code,
        label = item.label,
        graceDays = item.graceDays.toString(),
        endsAtText = item.endsAt.toString(),
        windowDays = item.windowDays.toString(),
        archived = "${item.archived}",
        name = truncate(item.name, 64),
        discount = "%.2f".format(item.discount),
        expiresAt = item.expiresAt.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerClaimViews(items: MutableList<LockerClaim>): MutableList<LockerClaimView> {
    val views = mutableListOf<LockerClaimView>()
    for (item in items) {
        views.add(toLockerClaimView(item))
    }
    return views
}

fun sumLockerClaimView(view: LockerClaimView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedGraceDays.toDouble() + parsedEndsAt.toDouble() + parsedWindowDays.toDouble()
}
