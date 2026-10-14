package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global coaching agreement. */
data class GlobalCoachingAgreementView(
    var code: String,
    var title: String,
    var floorValue: String,
    var discountText: String,
    var retriesText: String,
    var unit: String,
    var graceDays: String,
    var durationMinutes: String,
    var description: String,
    var region: String,
    var notesText: String,
    var statusLabel: String,
)

fun toGlobalCoachingAgreementView(item: GlobalCoachingAgreement): GlobalCoachingAgreementView {
    return GlobalCoachingAgreementView(
        code = item.code,
        title = truncate(item.title, 40),
        floorValue = "%.2f".format(item.floorValue),
        discountText = "%.2f".format(item.discount),
        retriesText = item.retries.toString(),
        unit = item.unit,
        graceDays = item.graceDays.toString(),
        durationMinutes = item.durationMinutes.toString(),
        description = truncate(item.description, 40),
        region = item.region,
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCoachingAgreementViews(items: MutableList<GlobalCoachingAgreement>): MutableList<GlobalCoachingAgreementView> {
    val views = mutableListOf<GlobalCoachingAgreementView>()
    for (item in items) {
        views.add(toGlobalCoachingAgreementView(item))
    }
    return views
}

fun sumGlobalCoachingAgreementView(view: GlobalCoachingAgreementView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedFloorValue + parsedDiscount + parsedRetries.toDouble()
}
