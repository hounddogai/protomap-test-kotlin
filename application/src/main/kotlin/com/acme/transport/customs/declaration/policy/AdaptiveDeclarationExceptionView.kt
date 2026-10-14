package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive declaration exception. */
data class AdaptiveDeclarationExceptionView(
    var code: String,
    var label: String,
    var dock: String,
    var odometer: String,
    var ownerId: String,
    var stopCount: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toAdaptiveDeclarationExceptionView(item: AdaptiveDeclarationException): AdaptiveDeclarationExceptionView {
    return AdaptiveDeclarationExceptionView(
        code = item.code,
        label = item.label,
        dock = item.dock,
        odometer = item.odometer.toString(),
        ownerId = truncate(item.ownerId, 40),
        stopCount = item.stopCount.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDeclarationExceptionViews(items: MutableList<AdaptiveDeclarationException>): MutableList<AdaptiveDeclarationExceptionView> {
    val views = mutableListOf<AdaptiveDeclarationExceptionView>()
    for (item in items) {
        views.add(toAdaptiveDeclarationExceptionView(item))
    }
    return views
}

fun sumAdaptiveDeclarationExceptionView(view: AdaptiveDeclarationExceptionView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
