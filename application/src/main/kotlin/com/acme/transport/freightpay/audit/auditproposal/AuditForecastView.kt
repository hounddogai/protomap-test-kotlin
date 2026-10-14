package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit forecast. */
data class AuditForecastView(
    var code: String,
    var label: String,
    var unit: String,
    var weight: String,
    var notes: String,
    var region: String,
    var descriptionText: String,
    var endsAt: String,
    var shiftCodeText: String,
    var priority: String,
    var statusLabel: String,
)

fun toAuditForecastView(item: AuditForecast): AuditForecastView {
    return AuditForecastView(
        code = item.code,
        label = truncate(item.label, 40),
        unit = item.unit,
        weight = "%.2f".format(item.weight),
        notes = truncate(item.notes, 120),
        region = truncate(item.region, 40),
        descriptionText = truncate(item.description, 120),
        endsAt = item.endsAt.toString(),
        shiftCodeText = item.shiftCode,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditForecastViews(items: MutableList<AuditForecast>): MutableList<AuditForecastView> {
    val views = mutableListOf<AuditForecastView>()
    for (item in items) {
        views.add(toAuditForecastView(item))
    }
    return views
}

fun sumAuditForecastView(view: AuditForecastView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedWeight + parsedEndsAt.toDouble() + parsedPriority.toDouble()
}
