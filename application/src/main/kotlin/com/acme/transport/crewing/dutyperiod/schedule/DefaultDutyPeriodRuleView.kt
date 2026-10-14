package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default duty period rule. */
data class DefaultDutyPeriodRuleView(
    var code: String,
    var name: String,
    var versionText: String,
    var checksum: String,
    var carrier: String,
    var vehicleIdText: String,
    var shiftCode: String,
    var quantityText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toDefaultDutyPeriodRuleView(item: DefaultDutyPeriodRule): DefaultDutyPeriodRuleView {
    return DefaultDutyPeriodRuleView(
        code = item.code,
        name = item.name,
        versionText = item.version.toString(),
        checksum = truncate(item.checksum, 64),
        carrier = item.carrier,
        vehicleIdText = item.vehicleId,
        shiftCode = truncate(item.shiftCode, 80),
        quantityText = item.quantity.toString(),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDutyPeriodRuleViews(items: MutableList<DefaultDutyPeriodRule>): MutableList<DefaultDutyPeriodRuleView> {
    val views = mutableListOf<DefaultDutyPeriodRuleView>()
    for (item in items) {
        views.add(toDefaultDutyPeriodRuleView(item))
    }
    return views
}

fun sumDefaultDutyPeriodRuleView(view: DefaultDutyPeriodRuleView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedVersion.toDouble() + parsedQuantity.toDouble() + parsedRate
}
