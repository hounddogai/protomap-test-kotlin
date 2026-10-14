package com.acme.transport.emissions.emissionfactor.factorsegment

/** How screens and exports show a tiered emission factor segment. */
data class TieredEmissionFactorSegmentView(
    var code: String,
    var name: String,
    var depotText: String,
    var cost: String,
    var batchSize: String,
    var revision: String,
    var scoreText: String,
    var timezone: String,
    var expiresAtText: String,
    var discount: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toTieredEmissionFactorSegmentView(item: TieredEmissionFactorSegment): TieredEmissionFactorSegmentView {
    return TieredEmissionFactorSegmentView(
        code = item.code,
        name = item.name,
        depotText = item.depot,
        cost = "%.2f".format(item.cost),
        batchSize = item.batchSize.toString(),
        revision = item.revision.toString(),
        scoreText = "%.2f".format(item.score),
        timezone = item.timezone,
        expiresAtText = item.expiresAt.toString(),
        discount = "%.2f".format(item.discount),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredEmissionFactorSegmentViews(items: MutableList<TieredEmissionFactorSegment>): MutableList<TieredEmissionFactorSegmentView> {
    val views = mutableListOf<TieredEmissionFactorSegmentView>()
    for (item in items) {
        views.add(toTieredEmissionFactorSegmentView(item))
    }
    return views
}

fun sumTieredEmissionFactorSegmentView(view: TieredEmissionFactorSegmentView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedCost + parsedBatchSize.toDouble() + parsedRevision.toDouble()
}
