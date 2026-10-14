package com.acme.transport.crewing.availability.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched availability agreement. */
data class BatchedAvailabilityAgreementView(
    var code: String,
    var nameText: String,
    var depot: String,
    var budget: String,
    var etaMinutesText: String,
    var shiftCode: String,
    var summary: String,
    var archived: String,
    var regionText: String,
    var retries: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toBatchedAvailabilityAgreementView(item: BatchedAvailabilityAgreement): BatchedAvailabilityAgreementView {
    return BatchedAvailabilityAgreementView(
        code = truncate(item.code, 40),
        nameText = item.name,
        depot = item.depot,
        budget = "%.2f".format(item.budget),
        etaMinutesText = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        summary = item.summary,
        archived = "${item.archived}",
        regionText = truncate(item.region, 40),
        retries = item.retries.toString(),
        descriptionText = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAvailabilityAgreementViews(items: MutableList<BatchedAvailabilityAgreement>): MutableList<BatchedAvailabilityAgreementView> {
    val views = mutableListOf<BatchedAvailabilityAgreementView>()
    for (item in items) {
        views.add(toBatchedAvailabilityAgreementView(item))
    }
    return views
}

fun sumBatchedAvailabilityAgreementView(view: BatchedAvailabilityAgreementView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedBudget + parsedEtaMinutes.toDouble() + parsedRetries.toDouble()
}
