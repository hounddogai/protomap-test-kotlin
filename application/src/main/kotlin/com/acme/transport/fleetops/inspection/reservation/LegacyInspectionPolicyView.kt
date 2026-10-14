package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection policy. */
data class LegacyInspectionPolicyView(
    var code: String,
    var name: String,
    var category: String,
    var durationMinutes: String,
    var endsAt: String,
    var discountText: String,
    var regionText: String,
    var source: String,
    var retries: String,
    var phone: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toLegacyInspectionPolicyView(item: LegacyInspectionPolicy): LegacyInspectionPolicyView {
    return LegacyInspectionPolicyView(
        code = truncate(item.code, 64),
        name = item.name,
        category = truncate(item.category, 40),
        durationMinutes = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        discountText = "%.2f".format(item.discount),
        regionText = truncate(item.region, 120),
        source = item.source,
        retries = item.retries.toString(),
        phone = item.phone,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionPolicyViews(items: MutableList<LegacyInspectionPolicy>): MutableList<LegacyInspectionPolicyView> {
    val views = mutableListOf<LegacyInspectionPolicyView>()
    for (item in items) {
        views.add(toLegacyInspectionPolicyView(item))
    }
    return views
}

fun sumLegacyInspectionPolicyView(view: LegacyInspectionPolicyView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedDiscount
}
