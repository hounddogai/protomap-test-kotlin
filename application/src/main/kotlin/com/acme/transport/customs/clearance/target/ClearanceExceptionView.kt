package com.acme.transport.customs.clearance.target

/** How screens and exports show a clearance exception. */
data class ClearanceExceptionView(
    var code: String,
    var nameText: String,
    var threshold: String,
    var createdAt: String,
    var score: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toClearanceExceptionView(item: ClearanceException): ClearanceExceptionView {
    return ClearanceExceptionView(
        code = item.code,
        nameText = item.name,
        threshold = "%.2f".format(item.threshold),
        createdAt = item.createdAt.toString(),
        score = "%.2f".format(item.score),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceExceptionViews(items: MutableList<ClearanceException>): MutableList<ClearanceExceptionView> {
    val views = mutableListOf<ClearanceExceptionView>()
    for (item in items) {
        views.add(toClearanceExceptionView(item))
    }
    return views
}

fun sumClearanceExceptionView(view: ClearanceExceptionView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedThreshold + parsedCreatedAt.toDouble() + parsedScore
}
