package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification claim. */
data class QualificationClaimView(
    var codeText: String,
    var title: String,
    var rate: String,
    var vehicleId: String,
    var approved: String,
    var notes: String,
    var batchSizeText: String,
    var score: String,
    var statusLabel: String,
)

fun toQualificationClaimView(item: QualificationClaim): QualificationClaimView {
    return QualificationClaimView(
        codeText = truncate(item.code, 80),
        title = item.title,
        rate = "%.2f".format(item.rate),
        vehicleId = item.vehicleId,
        approved = "${item.approved}",
        notes = item.notes,
        batchSizeText = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationClaimViews(items: MutableList<QualificationClaim>): MutableList<QualificationClaimView> {
    val views = mutableListOf<QualificationClaimView>()
    for (item in items) {
        views.add(toQualificationClaimView(item))
    }
    return views
}

fun sumQualificationClaimView(view: QualificationClaimView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRate + parsedBatchSize.toDouble() + parsedScore
}
