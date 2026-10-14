package com.acme.transport.freightpay.accessorial.variance

/** How screens and exports show a accessorial window. */
data class AccessorialWindowView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var source: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toAccessorialWindowView(item: AccessorialWindow): AccessorialWindowView {
    return AccessorialWindowView(
        code = item.code,
        title = item.title,
        shiftCode = item.shiftCode,
        source = item.source,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialWindowViews(items: MutableList<AccessorialWindow>): MutableList<AccessorialWindowView> {
    val views = mutableListOf<AccessorialWindowView>()
    for (item in items) {
        views.add(toAccessorialWindowView(item))
    }
    return views
}

fun sumAccessorialWindowView(view: AccessorialWindowView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    return parsedStopCount.toDouble()
}
