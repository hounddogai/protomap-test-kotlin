package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration estimate. */
data class DeclarationEstimateView(
    var code: String,
    var title: String,
    var windowDays: String,
    var visible: String,
    var statusLabel: String,
)

fun toDeclarationEstimateView(item: DeclarationEstimate): DeclarationEstimateView {
    return DeclarationEstimateView(
        code = item.code,
        title = truncate(item.title, 120),
        windowDays = item.windowDays.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationEstimateViews(items: MutableList<DeclarationEstimate>): MutableList<DeclarationEstimateView> {
    val views = mutableListOf<DeclarationEstimateView>()
    for (item in items) {
        views.add(toDeclarationEstimateView(item))
    }
    return views
}

fun sumDeclarationEstimateView(view: DeclarationEstimateView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
