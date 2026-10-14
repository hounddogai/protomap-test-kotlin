package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge draft. */
data class FuelSurchargeDraftView(
    var codeText: String,
    var label: String,
    var attempts: String,
    var driverIdText: String,
    var source: String,
    var currencyText: String,
    var createdAt: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toFuelSurchargeDraftView(item: FuelSurchargeDraft): FuelSurchargeDraftView {
    return FuelSurchargeDraftView(
        codeText = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        driverIdText = truncate(item.driverId, 80),
        source = truncate(item.source, 120),
        currencyText = item.currency,
        createdAt = item.createdAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeDraftViews(items: MutableList<FuelSurchargeDraft>): MutableList<FuelSurchargeDraftView> {
    val views = mutableListOf<FuelSurchargeDraftView>()
    for (item in items) {
        views.add(toFuelSurchargeDraftView(item))
    }
    return views
}

fun sumFuelSurchargeDraftView(view: FuelSurchargeDraftView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedAttempts.toDouble() + parsedCreatedAt.toDouble() + parsedThreshold
}
