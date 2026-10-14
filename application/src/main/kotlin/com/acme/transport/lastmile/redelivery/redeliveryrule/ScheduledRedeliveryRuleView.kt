package com.acme.transport.lastmile.redelivery.redeliveryrule

/** How screens and exports show a scheduled redelivery rule. */
data class ScheduledRedeliveryRuleView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var vehicleId: String,
    var revision: String,
    var statusLabel: String,
)

fun toScheduledRedeliveryRuleView(item: ScheduledRedeliveryRule): ScheduledRedeliveryRuleView {
    return ScheduledRedeliveryRuleView(
        code = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        vehicleId = item.vehicleId,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRedeliveryRuleViews(items: MutableList<ScheduledRedeliveryRule>): MutableList<ScheduledRedeliveryRuleView> {
    val views = mutableListOf<ScheduledRedeliveryRuleView>()
    for (item in items) {
        views.add(toScheduledRedeliveryRuleView(item))
    }
    return views
}

fun sumScheduledRedeliveryRuleView(view: ScheduledRedeliveryRuleView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
