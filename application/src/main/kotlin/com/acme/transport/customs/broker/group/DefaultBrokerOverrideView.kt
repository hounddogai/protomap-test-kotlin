package com.acme.transport.customs.broker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default broker override. */
data class DefaultBrokerOverrideView(
    var code: String,
    var labelText: String,
    var revision: String,
    var startsAt: String,
    var channel: String,
    var fuelLitersText: String,
    var notes: String,
    var discount: String,
    var limit: String,
    var timezoneText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDefaultBrokerOverrideView(item: DefaultBrokerOverride): DefaultBrokerOverrideView {
    return DefaultBrokerOverrideView(
        code = truncate(item.code, 64),
        labelText = item.label,
        revision = item.revision.toString(),
        startsAt = item.startsAt.toString(),
        channel = item.channel,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        notes = item.notes,
        discount = "%.2f".format(item.discount),
        limit = item.limit.toString(),
        timezoneText = item.timezone,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultBrokerOverrideViews(items: MutableList<DefaultBrokerOverride>): MutableList<DefaultBrokerOverrideView> {
    val views = mutableListOf<DefaultBrokerOverrideView>()
    for (item in items) {
        views.add(toDefaultBrokerOverrideView(item))
    }
    return views
}

fun sumDefaultBrokerOverrideView(view: DefaultBrokerOverrideView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedRevision.toDouble() + parsedStartsAt.toDouble() + parsedFuelLiters
}
