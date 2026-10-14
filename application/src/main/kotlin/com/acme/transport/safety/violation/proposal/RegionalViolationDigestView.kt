package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional violation digest. */
data class RegionalViolationDigestView(
    var code: String,
    var name: String,
    var segment: String,
    var retries: String,
    var pinned: String,
    var visible: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalViolationDigestView(item: RegionalViolationDigest): RegionalViolationDigestView {
    return RegionalViolationDigestView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        segment = item.segment,
        retries = item.retries.toString(),
        pinned = "${item.pinned}",
        visible = "${item.visible}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalViolationDigestViews(items: MutableList<RegionalViolationDigest>): MutableList<RegionalViolationDigestView> {
    val views = mutableListOf<RegionalViolationDigestView>()
    for (item in items) {
        views.add(toRegionalViolationDigestView(item))
    }
    return views
}

fun sumRegionalViolationDigestView(view: RegionalViolationDigestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRetries.toDouble() + parsedBudget
}
