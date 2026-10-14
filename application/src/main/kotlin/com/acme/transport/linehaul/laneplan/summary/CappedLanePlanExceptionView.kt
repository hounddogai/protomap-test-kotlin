package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped lane plan exception. */
data class CappedLanePlanExceptionView(
    var code: String,
    var nameText: String,
    var region: String,
    var ceiling: String,
    var vehicleId: String,
    var routeCodeText: String,
    var sourceText: String,
    var checksum: String,
    var discountText: String,
    var label: String,
    var retries: String,
    var statusLabel: String,
)

fun toCappedLanePlanExceptionView(item: CappedLanePlanException): CappedLanePlanExceptionView {
    return CappedLanePlanExceptionView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        region = item.region,
        ceiling = "%.2f".format(item.ceiling),
        vehicleId = item.vehicleId,
        routeCodeText = item.routeCode,
        sourceText = truncate(item.source, 120),
        checksum = item.checksum,
        discountText = "%.2f".format(item.discount),
        label = truncate(item.label, 80),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLanePlanExceptionViews(items: MutableList<CappedLanePlanException>): MutableList<CappedLanePlanExceptionView> {
    val views = mutableListOf<CappedLanePlanExceptionView>()
    for (item in items) {
        views.add(toCappedLanePlanExceptionView(item))
    }
    return views
}

fun sumCappedLanePlanExceptionView(view: CappedLanePlanExceptionView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedCeiling + parsedDiscount + parsedRetries.toDouble()
}
