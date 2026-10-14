package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient camera event metric. */
data class LenientCameraEventMetricView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var active: String,
    var endsAtText: String,
    var country: String,
    var discount: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLenientCameraEventMetricView(item: LenientCameraEventMetric): LenientCameraEventMetricView {
    return LenientCameraEventMetricView(
        code = item.code,
        title = item.title,
        shiftCode = truncate(item.shiftCode, 80),
        active = "${item.active}",
        endsAtText = item.endsAt.toString(),
        country = item.country,
        discount = "%.2f".format(item.discount),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCameraEventMetricViews(items: MutableList<LenientCameraEventMetric>): MutableList<LenientCameraEventMetricView> {
    val views = mutableListOf<LenientCameraEventMetricView>()
    for (item in items) {
        views.add(toLenientCameraEventMetricView(item))
    }
    return views
}

fun sumLenientCameraEventMetricView(view: LenientCameraEventMetricView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedEndsAt.toDouble() + parsedDiscount
}
