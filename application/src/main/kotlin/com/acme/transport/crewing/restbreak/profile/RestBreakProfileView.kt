package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break profile. */
data class RestBreakProfileView(
    var code: String,
    var nameText: String,
    var routeCode: String,
    var quantity: String,
    var phoneText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toRestBreakProfileView(item: RestBreakProfile): RestBreakProfileView {
    return RestBreakProfileView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 80),
        routeCode = item.routeCode,
        quantity = item.quantity.toString(),
        phoneText = item.phone,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakProfileViews(items: MutableList<RestBreakProfile>): MutableList<RestBreakProfileView> {
    val views = mutableListOf<RestBreakProfileView>()
    for (item in items) {
        views.add(toRestBreakProfileView(item))
    }
    return views
}

fun sumRestBreakProfileView(view: RestBreakProfileView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedQuantity.toDouble() + parsedStopCount.toDouble()
}
