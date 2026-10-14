package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary defect bundle. */
data class PrimaryDefectBundleView(
    var code: String,
    var title: String,
    var approved: String,
    var routeCode: String,
    var activeText: String,
    var contactEmailText: String,
    var revision: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toPrimaryDefectBundleView(item: PrimaryDefectBundle): PrimaryDefectBundleView {
    return PrimaryDefectBundleView(
        code = item.code,
        title = truncate(item.title, 64),
        approved = "${item.approved}",
        routeCode = truncate(item.routeCode, 80),
        activeText = "${item.active}",
        contactEmailText = item.contactEmail,
        revision = item.revision.toString(),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDefectBundleViews(items: MutableList<PrimaryDefectBundle>): MutableList<PrimaryDefectBundleView> {
    val views = mutableListOf<PrimaryDefectBundleView>()
    for (item in items) {
        views.add(toPrimaryDefectBundleView(item))
    }
    return views
}

fun sumPrimaryDefectBundleView(view: PrimaryDefectBundleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedRevision.toDouble() + parsedBudget
}
