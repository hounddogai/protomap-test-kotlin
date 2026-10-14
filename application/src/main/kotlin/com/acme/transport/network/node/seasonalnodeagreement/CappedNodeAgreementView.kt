package com.acme.transport.network.node.seasonalnodeagreement

/** How screens and exports show a capped node agreement. */
data class CappedNodeAgreementView(
    var codeText: String,
    var title: String,
    var attemptsText: String,
    var graceDaysText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toCappedNodeAgreementView(item: CappedNodeAgreement): CappedNodeAgreementView {
    return CappedNodeAgreementView(
        codeText = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        graceDaysText = item.graceDays.toString(),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedNodeAgreementViews(items: MutableList<CappedNodeAgreement>): MutableList<CappedNodeAgreementView> {
    val views = mutableListOf<CappedNodeAgreementView>()
    for (item in items) {
        views.add(toCappedNodeAgreementView(item))
    }
    return views
}

fun sumCappedNodeAgreementView(view: CappedNodeAgreementView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
