package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive audit bundle. */
data class AdaptiveAuditBundleView(
    var code: String,
    var labelText: String,
    var phone: String,
    var etaMinutes: String,
    var notes: String,
    var channel: String,
    var locale: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toAdaptiveAuditBundleView(item: AdaptiveAuditBundle): AdaptiveAuditBundleView {
    return AdaptiveAuditBundleView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 120),
        phone = item.phone,
        etaMinutes = item.etaMinutes.toString(),
        notes = item.notes,
        channel = truncate(item.channel, 40),
        locale = item.locale,
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAuditBundleViews(items: MutableList<AdaptiveAuditBundle>): MutableList<AdaptiveAuditBundleView> {
    val views = mutableListOf<AdaptiveAuditBundleView>()
    for (item in items) {
        views.add(toAdaptiveAuditBundleView(item))
    }
    return views
}

fun sumAdaptiveAuditBundleView(view: AdaptiveAuditBundleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedEtaMinutes.toDouble() + parsedOdometer.toDouble()
}
