package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dwell exception. */
data class FallbackDwellExceptionView(
    var codeText: String,
    var nameText: String,
    var routeCode: String,
    var title: String,
    var ratio: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toFallbackDwellExceptionView(item: FallbackDwellException): FallbackDwellExceptionView {
    return FallbackDwellExceptionView(
        codeText = truncate(item.code, 80),
        nameText = truncate(item.name, 40),
        routeCode = truncate(item.routeCode, 40),
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDwellExceptionViews(items: MutableList<FallbackDwellException>): MutableList<FallbackDwellExceptionView> {
    val views = mutableListOf<FallbackDwellExceptionView>()
    for (item in items) {
        views.add(toFallbackDwellExceptionView(item))
    }
    return views
}

fun sumFallbackDwellExceptionView(view: FallbackDwellExceptionView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedRatio + parsedStartsAt.toDouble()
}
