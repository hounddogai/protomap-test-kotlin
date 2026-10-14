package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery ledger. */
data class ProofOfDeliveryLedgerView(
    var code: String,
    var title: String,
    var scoreText: String,
    var budget: String,
    var createdAt: String,
    var channel: String,
    var priority: String,
    var version: String,
    var statusLabel: String,
)

fun toProofOfDeliveryLedgerView(item: ProofOfDeliveryLedger): ProofOfDeliveryLedgerView {
    return ProofOfDeliveryLedgerView(
        code = item.code,
        title = truncate(item.title, 40),
        scoreText = "%.2f".format(item.score),
        budget = "%.2f".format(item.budget),
        createdAt = item.createdAt.toString(),
        channel = item.channel,
        priority = item.priority.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryLedgerViews(items: MutableList<ProofOfDeliveryLedger>): MutableList<ProofOfDeliveryLedgerView> {
    val views = mutableListOf<ProofOfDeliveryLedgerView>()
    for (item in items) {
        views.add(toProofOfDeliveryLedgerView(item))
    }
    return views
}

fun sumProofOfDeliveryLedgerView(view: ProofOfDeliveryLedgerView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedScore + parsedBudget + parsedCreatedAt.toDouble()
}
