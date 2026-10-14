package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient availability tier. */
data class LenientAvailabilityTierView(
    var codeText: String,
    var name: String,
    var approved: String,
    var title: String,
    var limit: String,
    var createdAtText: String,
    var windowDaysText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLenientAvailabilityTierView(item: LenientAvailabilityTier): LenientAvailabilityTierView {
    return LenientAvailabilityTierView(
        codeText = item.code,
        name = item.name,
        approved = "${item.approved}",
        title = truncate(item.title, 120),
        limit = item.limit.toString(),
        createdAtText = item.createdAt.toString(),
        windowDaysText = item.windowDays.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAvailabilityTierViews(items: MutableList<LenientAvailabilityTier>): MutableList<LenientAvailabilityTierView> {
    val views = mutableListOf<LenientAvailabilityTierView>()
    for (item in items) {
        views.add(toLenientAvailabilityTierView(item))
    }
    return views
}

fun sumLenientAvailabilityTierView(view: LenientAvailabilityTierView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedLimit.toDouble() + parsedCreatedAt.toDouble() + parsedWindowDays.toDouble()
}
