package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling violation group. */
data class RollingViolationGroupView(
    var code: String,
    var label: String,
    var reviewer: String,
    var versionText: String,
    var currency: String,
    var approved: String,
    var amountText: String,
    var dockText: String,
    var loadKgText: String,
    var timezone: String,
    var minimum: String,
    var statusLabel: String,
)

fun toRollingViolationGroupView(item: RollingViolationGroup): RollingViolationGroupView {
    return RollingViolationGroupView(
        code = item.code,
        label = truncate(item.label, 40),
        reviewer = item.reviewer,
        versionText = item.version.toString(),
        currency = item.currency,
        approved = "${item.approved}",
        amountText = "%.2f".format(item.amount),
        dockText = item.dock,
        loadKgText = "%.2f".format(item.loadKg),
        timezone = item.timezone,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationGroupViews(items: MutableList<RollingViolationGroup>): MutableList<RollingViolationGroupView> {
    val views = mutableListOf<RollingViolationGroupView>()
    for (item in items) {
        views.add(toRollingViolationGroupView(item))
    }
    return views
}

fun sumRollingViolationGroupView(view: RollingViolationGroupView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedVersion.toDouble() + parsedAmount + parsedLoadKg
}
