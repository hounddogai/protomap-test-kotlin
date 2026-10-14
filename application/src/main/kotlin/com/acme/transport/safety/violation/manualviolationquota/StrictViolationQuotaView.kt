package com.acme.transport.safety.violation.manualviolationquota

/** How screens and exports show a strict violation quota. */
data class StrictViolationQuotaView(
    var code: String,
    var title: String,
    var graceDays: String,
    var etaMinutes: String,
    var marginText: String,
    var weight: String,
    var expiresAtText: String,
    var windowDays: String,
    var revisionText: String,
    var fuelLiters: String,
    var label: String,
    var statusLabel: String,
)

fun toStrictViolationQuotaView(item: StrictViolationQuota): StrictViolationQuotaView {
    return StrictViolationQuotaView(
        code = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        etaMinutes = item.etaMinutes.toString(),
        marginText = "%.2f".format(item.margin),
        weight = "%.2f".format(item.weight),
        expiresAtText = item.expiresAt.toString(),
        windowDays = item.windowDays.toString(),
        revisionText = item.revision.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictViolationQuotaViews(items: MutableList<StrictViolationQuota>): MutableList<StrictViolationQuotaView> {
    val views = mutableListOf<StrictViolationQuotaView>()
    for (item in items) {
        views.add(toStrictViolationQuotaView(item))
    }
    return views
}

fun sumStrictViolationQuotaView(view: StrictViolationQuotaView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedMargin
}
