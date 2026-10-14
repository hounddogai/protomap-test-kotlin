package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy qualification policy. */
data class LegacyQualificationPolicyView(
    var codeText: String,
    var name: String,
    var carrier: String,
    var visibleText: String,
    var category: String,
    var priority: String,
    var statusLabel: String,
)

fun toLegacyQualificationPolicyView(item: LegacyQualificationPolicy): LegacyQualificationPolicyView {
    return LegacyQualificationPolicyView(
        codeText = item.code,
        name = truncate(item.name, 80),
        carrier = truncate(item.carrier, 40),
        visibleText = "${item.visible}",
        category = item.category,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyQualificationPolicyViews(items: MutableList<LegacyQualificationPolicy>): MutableList<LegacyQualificationPolicyView> {
    val views = mutableListOf<LegacyQualificationPolicyView>()
    for (item in items) {
        views.add(toLegacyQualificationPolicyView(item))
    }
    return views
}

fun sumLegacyQualificationPolicyView(view: LegacyQualificationPolicyView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
