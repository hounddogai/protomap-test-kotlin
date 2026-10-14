package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary violation preference. */
data class PrimaryViolationPreferenceView(
    var codeText: String,
    var title: String,
    var unit: String,
    var etaMinutes: String,
    var priority: String,
    var currency: String,
    var category: String,
    var statusLabel: String,
)

fun toPrimaryViolationPreferenceView(item: PrimaryViolationPreference): PrimaryViolationPreferenceView {
    return PrimaryViolationPreferenceView(
        codeText = truncate(item.code, 120),
        title = truncate(item.title, 120),
        unit = item.unit,
        etaMinutes = item.etaMinutes.toString(),
        priority = item.priority.toString(),
        currency = item.currency,
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryViolationPreferenceViews(items: MutableList<PrimaryViolationPreference>): MutableList<PrimaryViolationPreferenceView> {
    val views = mutableListOf<PrimaryViolationPreferenceView>()
    for (item in items) {
        views.add(toPrimaryViolationPreferenceView(item))
    }
    return views
}

fun sumPrimaryViolationPreferenceView(view: PrimaryViolationPreferenceView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedEtaMinutes.toDouble() + parsedPriority.toDouble()
}
