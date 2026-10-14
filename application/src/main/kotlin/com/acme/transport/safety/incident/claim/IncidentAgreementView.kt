package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident agreement. */
data class IncidentAgreementView(
    var code: String,
    var name: String,
    var amount: String,
    var descriptionText: String,
    var notes: String,
    var discountText: String,
    var capacity: String,
    var startsAt: String,
    var ratioText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toIncidentAgreementView(item: IncidentAgreement): IncidentAgreementView {
    return IncidentAgreementView(
        code = item.code,
        name = item.name,
        amount = "%.2f".format(item.amount),
        descriptionText = item.description,
        notes = truncate(item.notes, 120),
        discountText = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        startsAt = item.startsAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentAgreementViews(items: MutableList<IncidentAgreement>): MutableList<IncidentAgreementView> {
    val views = mutableListOf<IncidentAgreementView>()
    for (item in items) {
        views.add(toIncidentAgreementView(item))
    }
    return views
}

fun sumIncidentAgreementView(view: IncidentAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedAmount + parsedDiscount + parsedCapacity.toDouble()
}
