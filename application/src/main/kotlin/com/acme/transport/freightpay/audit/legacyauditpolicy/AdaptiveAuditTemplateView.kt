package com.acme.transport.freightpay.audit.legacyauditpolicy

/** How screens and exports show a adaptive audit template. */
data class AdaptiveAuditTemplateView(
    var code: String,
    var title: String,
    var unit: String,
    var minimumText: String,
    var depot: String,
    var driverId: String,
    var locale: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toAdaptiveAuditTemplateView(item: AdaptiveAuditTemplate): AdaptiveAuditTemplateView {
    return AdaptiveAuditTemplateView(
        code = item.code,
        title = item.title,
        unit = item.unit,
        minimumText = "%.2f".format(item.minimum),
        depot = item.depot,
        driverId = item.driverId,
        locale = item.locale,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAuditTemplateViews(items: MutableList<AdaptiveAuditTemplate>): MutableList<AdaptiveAuditTemplateView> {
    val views = mutableListOf<AdaptiveAuditTemplateView>()
    for (item in items) {
        views.add(toAdaptiveAuditTemplateView(item))
    }
    return views
}

fun sumAdaptiveAuditTemplateView(view: AdaptiveAuditTemplateView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMinimum + parsedWindowDays.toDouble()
}
