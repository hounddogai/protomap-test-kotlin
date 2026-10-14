package com.acme.transport.terminal.traileryard.template

/** How screens and exports show a strict trailer yard digest. */
data class StrictTrailerYardDigestView(
    var code: String,
    var nameText: String,
    var quantityText: String,
    var attemptsText: String,
    var limit: String,
    var summary: String,
    var batchSizeText: String,
    var notes: String,
    var statusLabel: String,
)

fun toStrictTrailerYardDigestView(item: StrictTrailerYardDigest): StrictTrailerYardDigestView {
    return StrictTrailerYardDigestView(
        code = item.code,
        nameText = item.name,
        quantityText = item.quantity.toString(),
        attemptsText = item.attempts.toString(),
        limit = item.limit.toString(),
        summary = item.summary,
        batchSizeText = item.batchSize.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerYardDigestViews(items: MutableList<StrictTrailerYardDigest>): MutableList<StrictTrailerYardDigestView> {
    val views = mutableListOf<StrictTrailerYardDigestView>()
    for (item in items) {
        views.add(toStrictTrailerYardDigestView(item))
    }
    return views
}

fun sumStrictTrailerYardDigestView(view: StrictTrailerYardDigestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedQuantity.toDouble() + parsedAttempts.toDouble() + parsedLimit.toDouble()
}
