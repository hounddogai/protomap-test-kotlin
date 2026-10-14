package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tariff exception. */
data class LenientTariffExceptionView(
    var code: String,
    var label: String,
    var laneText: String,
    var batchSize: String,
    var marginText: String,
    var durationMinutes: String,
    var ownerId: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toLenientTariffExceptionView(item: LenientTariffException): LenientTariffExceptionView {
    return LenientTariffExceptionView(
        code = item.code,
        label = item.label,
        laneText = item.lane,
        batchSize = item.batchSize.toString(),
        marginText = "%.2f".format(item.margin),
        durationMinutes = item.durationMinutes.toString(),
        ownerId = truncate(item.ownerId, 120),
        routeCodeText = truncate(item.routeCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTariffExceptionViews(items: MutableList<LenientTariffException>): MutableList<LenientTariffExceptionView> {
    val views = mutableListOf<LenientTariffExceptionView>()
    for (item in items) {
        views.add(toLenientTariffExceptionView(item))
    }
    return views
}

fun sumLenientTariffExceptionView(view: LenientTariffExceptionView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedBatchSize.toDouble() + parsedMargin + parsedDurationMinutes.toDouble()
}
