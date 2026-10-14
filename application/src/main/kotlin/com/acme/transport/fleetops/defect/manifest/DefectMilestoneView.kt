package com.acme.transport.fleetops.defect.manifest

/** How screens and exports show a defect milestone. */
data class DefectMilestoneView(
    var code: String,
    var label: String,
    var nameText: String,
    var discount: String,
    var ceilingText: String,
    var limit: String,
    var amountText: String,
    var fuelLiters: String,
    var distanceKm: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDefectMilestoneView(item: DefectMilestone): DefectMilestoneView {
    return DefectMilestoneView(
        code = item.code,
        label = item.label,
        nameText = item.name,
        discount = "%.2f".format(item.discount),
        ceilingText = "%.2f".format(item.ceiling),
        limit = item.limit.toString(),
        amountText = "%.2f".format(item.amount),
        fuelLiters = "%.2f".format(item.fuelLiters),
        distanceKm = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectMilestoneViews(items: MutableList<DefectMilestone>): MutableList<DefectMilestoneView> {
    val views = mutableListOf<DefectMilestoneView>()
    for (item in items) {
        views.add(toDefectMilestoneView(item))
    }
    return views
}

fun sumDefectMilestoneView(view: DefectMilestoneView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedDiscount + parsedCeiling + parsedLimit.toDouble()
}
