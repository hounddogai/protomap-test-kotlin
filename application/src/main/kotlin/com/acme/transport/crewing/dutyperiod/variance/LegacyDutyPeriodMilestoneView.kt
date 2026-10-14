package com.acme.transport.crewing.dutyperiod.variance

/** How screens and exports show a legacy duty period milestone. */
data class LegacyDutyPeriodMilestoneView(
    var codeText: String,
    var label: String,
    var rate: String,
    var checksum: String,
    var timezone: String,
    var quantity: String,
    var minimumText: String,
    var descriptionText: String,
    var amount: String,
    var statusLabel: String,
)

fun toLegacyDutyPeriodMilestoneView(item: LegacyDutyPeriodMilestone): LegacyDutyPeriodMilestoneView {
    return LegacyDutyPeriodMilestoneView(
        codeText = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        checksum = item.checksum,
        timezone = item.timezone,
        quantity = item.quantity.toString(),
        minimumText = "%.2f".format(item.minimum),
        descriptionText = item.description,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDutyPeriodMilestoneViews(items: MutableList<LegacyDutyPeriodMilestone>): MutableList<LegacyDutyPeriodMilestoneView> {
    val views = mutableListOf<LegacyDutyPeriodMilestoneView>()
    for (item in items) {
        views.add(toLegacyDutyPeriodMilestoneView(item))
    }
    return views
}

fun sumLegacyDutyPeriodMilestoneView(view: LegacyDutyPeriodMilestoneView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedRate + parsedQuantity.toDouble() + parsedMinimum
}
