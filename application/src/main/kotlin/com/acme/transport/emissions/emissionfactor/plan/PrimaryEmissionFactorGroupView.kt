package com.acme.transport.emissions.emissionfactor.plan

/** How screens and exports show a primary emission factor group. */
data class PrimaryEmissionFactorGroupView(
    var codeText: String,
    var nameText: String,
    var amount: String,
    var trailerId: String,
    var batchSize: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toPrimaryEmissionFactorGroupView(item: PrimaryEmissionFactorGroup): PrimaryEmissionFactorGroupView {
    return PrimaryEmissionFactorGroupView(
        codeText = item.code,
        nameText = item.name,
        amount = "%.2f".format(item.amount),
        trailerId = item.trailerId,
        batchSize = item.batchSize.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEmissionFactorGroupViews(items: MutableList<PrimaryEmissionFactorGroup>): MutableList<PrimaryEmissionFactorGroupView> {
    val views = mutableListOf<PrimaryEmissionFactorGroupView>()
    for (item in items) {
        views.add(toPrimaryEmissionFactorGroupView(item))
    }
    return views
}

fun sumPrimaryEmissionFactorGroupView(view: PrimaryEmissionFactorGroupView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedAmount + parsedBatchSize.toDouble()
}
