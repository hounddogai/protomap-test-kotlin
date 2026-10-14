package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient qualification claim. */
data class LenientQualificationClaimView(
    var codeText: String,
    var name: String,
    var createdAt: String,
    var scoreText: String,
    var channelText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLenientQualificationClaimView(item: LenientQualificationClaim): LenientQualificationClaimView {
    return LenientQualificationClaimView(
        codeText = item.code,
        name = item.name,
        createdAt = item.createdAt.toString(),
        scoreText = "%.2f".format(item.score),
        channelText = truncate(item.channel, 40),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientQualificationClaimViews(items: MutableList<LenientQualificationClaim>): MutableList<LenientQualificationClaimView> {
    val views = mutableListOf<LenientQualificationClaimView>()
    for (item in items) {
        views.add(toLenientQualificationClaimView(item))
    }
    return views
}

fun sumLenientQualificationClaimView(view: LenientQualificationClaimView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCreatedAt.toDouble() + parsedScore + parsedUpdatedAt.toDouble()
}
