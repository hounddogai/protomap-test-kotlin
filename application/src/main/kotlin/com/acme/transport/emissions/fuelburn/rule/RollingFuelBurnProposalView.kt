package com.acme.transport.emissions.fuelburn.rule

/** How screens and exports show a rolling fuel burn proposal. */
data class RollingFuelBurnProposalView(
    var codeText: String,
    var name: String,
    var title: String,
    var discountText: String,
    var quantity: String,
    var batchSize: String,
    var reviewer: String,
    var sequence: String,
    var floorValue: String,
    var trailerId: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toRollingFuelBurnProposalView(item: RollingFuelBurnProposal): RollingFuelBurnProposalView {
    return RollingFuelBurnProposalView(
        codeText = item.code,
        name = item.name,
        title = item.title,
        discountText = "%.2f".format(item.discount),
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        reviewer = item.reviewer,
        sequence = item.sequence.toString(),
        floorValue = "%.2f".format(item.floorValue),
        trailerId = item.trailerId,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingFuelBurnProposalViews(items: MutableList<RollingFuelBurnProposal>): MutableList<RollingFuelBurnProposalView> {
    val views = mutableListOf<RollingFuelBurnProposalView>()
    for (item in items) {
        views.add(toRollingFuelBurnProposalView(item))
    }
    return views
}

fun sumRollingFuelBurnProposalView(view: RollingFuelBurnProposalView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDiscount + parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
