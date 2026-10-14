package com.acme.transport.linehaul.tender.profile

/** How screens and exports show a tender exception. */
data class TenderExceptionView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var priority: String,
    var rate: String,
    var odometer: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toTenderExceptionView(item: TenderException): TenderExceptionView {
    return TenderExceptionView(
        code = item.code,
        nameText = item.name,
        stopCount = item.stopCount.toString(),
        priority = item.priority.toString(),
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderExceptionViews(items: MutableList<TenderException>): MutableList<TenderExceptionView> {
    val views = mutableListOf<TenderExceptionView>()
    for (item in items) {
        views.add(toTenderExceptionView(item))
    }
    return views
}

fun sumTenderExceptionView(view: TenderExceptionView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedStopCount.toDouble() + parsedPriority.toDouble() + parsedRate
}
