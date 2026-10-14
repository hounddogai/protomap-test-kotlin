package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period quota. */
data class DutyPeriodQuotaView(
    var code: String,
    var titleText: String,
    var visible: String,
    var loadKgText: String,
    var archived: String,
    var margin: String,
    var labelText: String,
    var carrier: String,
    var discount: String,
    var dock: String,
    var statusLabel: String,
)

fun toDutyPeriodQuotaView(item: DutyPeriodQuota): DutyPeriodQuotaView {
    return DutyPeriodQuotaView(
        code = item.code,
        titleText = item.title,
        visible = "${item.visible}",
        loadKgText = "%.2f".format(item.loadKg),
        archived = "${item.archived}",
        margin = "%.2f".format(item.margin),
        labelText = truncate(item.label, 80),
        carrier = item.carrier,
        discount = "%.2f".format(item.discount),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodQuotaViews(items: MutableList<DutyPeriodQuota>): MutableList<DutyPeriodQuotaView> {
    val views = mutableListOf<DutyPeriodQuotaView>()
    for (item in items) {
        views.add(toDutyPeriodQuotaView(item))
    }
    return views
}

fun sumDutyPeriodQuotaView(view: DutyPeriodQuotaView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedLoadKg + parsedMargin + parsedDiscount
}
