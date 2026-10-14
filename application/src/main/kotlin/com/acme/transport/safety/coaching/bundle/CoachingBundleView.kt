package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching bundle. */
data class CoachingBundleView(
    var code: String,
    var name: String,
    var label: String,
    var weight: String,
    var attempts: String,
    var priority: String,
    var statusLabel: String,
)

fun toCoachingBundleView(item: CoachingBundle): CoachingBundleView {
    return CoachingBundleView(
        code = truncate(item.code, 80),
        name = item.name,
        label = item.label,
        weight = "%.2f".format(item.weight),
        attempts = item.attempts.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingBundleViews(items: MutableList<CoachingBundle>): MutableList<CoachingBundleView> {
    val views = mutableListOf<CoachingBundleView>()
    for (item in items) {
        views.add(toCoachingBundleView(item))
    }
    return views
}

fun sumCoachingBundleView(view: CoachingBundleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedWeight + parsedAttempts.toDouble() + parsedPriority.toDouble()
}
