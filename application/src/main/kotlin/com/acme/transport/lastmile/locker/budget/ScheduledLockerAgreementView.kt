package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled locker agreement. */
data class ScheduledLockerAgreementView(
    var codeText: String,
    var title: String,
    var limit: String,
    var summary: String,
    var etaMinutes: String,
    var fuelLitersText: String,
    var attempts: String,
    var country: String,
    var statusLabel: String,
)

fun toScheduledLockerAgreementView(item: ScheduledLockerAgreement): ScheduledLockerAgreementView {
    return ScheduledLockerAgreementView(
        codeText = truncate(item.code, 64),
        title = item.title,
        limit = item.limit.toString(),
        summary = item.summary,
        etaMinutes = item.etaMinutes.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        attempts = item.attempts.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLockerAgreementViews(items: MutableList<ScheduledLockerAgreement>): MutableList<ScheduledLockerAgreementView> {
    val views = mutableListOf<ScheduledLockerAgreementView>()
    for (item in items) {
        views.add(toScheduledLockerAgreementView(item))
    }
    return views
}

fun sumScheduledLockerAgreementView(view: ScheduledLockerAgreementView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLimit.toDouble() + parsedEtaMinutes.toDouble() + parsedFuelLiters
}
