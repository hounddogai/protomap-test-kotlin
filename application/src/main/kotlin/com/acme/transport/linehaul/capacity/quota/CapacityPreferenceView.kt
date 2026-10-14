package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity preference. */
data class CapacityPreferenceView(
    var codeText: String,
    var nameText: String,
    var rateText: String,
    var expiresAt: String,
    var reviewer: String,
    var attempts: String,
    var category: String,
    var statusLabel: String,
)

fun toCapacityPreferenceView(item: CapacityPreference): CapacityPreferenceView {
    return CapacityPreferenceView(
        codeText = item.code,
        nameText = item.name,
        rateText = "%.2f".format(item.rate),
        expiresAt = item.expiresAt.toString(),
        reviewer = item.reviewer,
        attempts = item.attempts.toString(),
        category = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityPreferenceViews(items: MutableList<CapacityPreference>): MutableList<CapacityPreferenceView> {
    val views = mutableListOf<CapacityPreferenceView>()
    for (item in items) {
        views.add(toCapacityPreferenceView(item))
    }
    return views
}

fun sumCapacityPreferenceView(view: CapacityPreferenceView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedRate + parsedExpiresAt.toDouble() + parsedAttempts.toDouble()
}
