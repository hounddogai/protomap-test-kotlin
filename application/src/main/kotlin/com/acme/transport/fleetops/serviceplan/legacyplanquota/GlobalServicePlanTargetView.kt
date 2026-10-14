package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global service plan target. */
data class GlobalServicePlanTargetView(
    var code: String,
    var labelText: String,
    var title: String,
    var depot: String,
    var pinnedText: String,
    var reviewerText: String,
    var locale: String,
    var amount: String,
    var statusLabel: String,
)

fun toGlobalServicePlanTargetView(item: GlobalServicePlanTarget): GlobalServicePlanTargetView {
    return GlobalServicePlanTargetView(
        code = item.code,
        labelText = item.label,
        title = item.title,
        depot = item.depot,
        pinnedText = "${item.pinned}",
        reviewerText = truncate(item.reviewer, 64),
        locale = item.locale,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalServicePlanTargetViews(items: MutableList<GlobalServicePlanTarget>): MutableList<GlobalServicePlanTargetView> {
    val views = mutableListOf<GlobalServicePlanTargetView>()
    for (item in items) {
        views.add(toGlobalServicePlanTargetView(item))
    }
    return views
}

fun sumGlobalServicePlanTargetView(view: GlobalServicePlanTargetView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
