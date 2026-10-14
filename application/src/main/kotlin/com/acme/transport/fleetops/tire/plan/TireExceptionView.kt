package com.acme.transport.fleetops.tire.plan

/** How screens and exports show a tire exception. */
data class TireExceptionView(
    var code: String,
    var label: String,
    var channel: String,
    var approvedText: String,
    var timezone: String,
    var region: String,
    var rate: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTireExceptionView(item: TireException): TireExceptionView {
    return TireExceptionView(
        code = item.code,
        label = item.label,
        channel = item.channel,
        approvedText = "${item.approved}",
        timezone = item.timezone,
        region = item.region,
        rate = "%.2f".format(item.rate),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireExceptionViews(items: MutableList<TireException>): MutableList<TireExceptionView> {
    val views = mutableListOf<TireExceptionView>()
    for (item in items) {
        views.add(toTireExceptionView(item))
    }
    return views
}

fun sumTireExceptionView(view: TireExceptionView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedRate + parsedAttempts.toDouble()
}
