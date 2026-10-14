package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker template. */
data class BrokerTemplateView(
    var code: String,
    var label: String,
    var loadKg: String,
    var attempts: String,
    var endsAt: String,
    var contactEmail: String,
    var odometer: String,
    var discountText: String,
    var statusLabel: String,
)

fun toBrokerTemplateView(item: BrokerTemplate): BrokerTemplateView {
    return BrokerTemplateView(
        code = truncate(item.code, 80),
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        attempts = item.attempts.toString(),
        endsAt = item.endsAt.toString(),
        contactEmail = truncate(item.contactEmail, 64),
        odometer = item.odometer.toString(),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerTemplateViews(items: MutableList<BrokerTemplate>): MutableList<BrokerTemplateView> {
    val views = mutableListOf<BrokerTemplateView>()
    for (item in items) {
        views.add(toBrokerTemplateView(item))
    }
    return views
}

fun sumBrokerTemplateView(view: BrokerTemplateView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedLoadKg + parsedAttempts.toDouble() + parsedEndsAt.toDouble()
}
