package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor review. */
data class EmissionFactorReviewView(
    var code: String,
    var name: String,
    var archivedText: String,
    var endsAt: String,
    var retries: String,
    var country: String,
    var loadKg: String,
    var carrier: String,
    var label: String,
    var score: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toEmissionFactorReviewView(item: EmissionFactorReview): EmissionFactorReviewView {
    return EmissionFactorReviewView(
        code = item.code,
        name = item.name,
        archivedText = "${item.archived}",
        endsAt = item.endsAt.toString(),
        retries = item.retries.toString(),
        country = item.country,
        loadKg = "%.2f".format(item.loadKg),
        carrier = truncate(item.carrier, 40),
        label = item.label,
        score = "%.2f".format(item.score),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorReviewViews(items: MutableList<EmissionFactorReview>): MutableList<EmissionFactorReviewView> {
    val views = mutableListOf<EmissionFactorReviewView>()
    for (item in items) {
        views.add(toEmissionFactorReviewView(item))
    }
    return views
}

fun sumEmissionFactorReviewView(view: EmissionFactorReviewView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble() + parsedLoadKg
}
