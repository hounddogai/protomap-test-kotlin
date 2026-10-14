package com.acme.transport.crewing.dutyperiod.target

/** How screens and exports show a default duty period rollup. */
data class DefaultDutyPeriodRollupView(
    var code: String,
    var name: String,
    var depot: String,
    var title: String,
    var visible: String,
    var contactEmail: String,
    var amount: String,
    var statusLabel: String,
)

fun toDefaultDutyPeriodRollupView(item: DefaultDutyPeriodRollup): DefaultDutyPeriodRollupView {
    return DefaultDutyPeriodRollupView(
        code = item.code,
        name = item.name,
        depot = item.depot,
        title = item.title,
        visible = "${item.visible}",
        contactEmail = item.contactEmail,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDutyPeriodRollupViews(items: MutableList<DefaultDutyPeriodRollup>): MutableList<DefaultDutyPeriodRollupView> {
    val views = mutableListOf<DefaultDutyPeriodRollupView>()
    for (item in items) {
        views.add(toDefaultDutyPeriodRollupView(item))
    }
    return views
}

fun sumDefaultDutyPeriodRollupView(view: DefaultDutyPeriodRollupView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
