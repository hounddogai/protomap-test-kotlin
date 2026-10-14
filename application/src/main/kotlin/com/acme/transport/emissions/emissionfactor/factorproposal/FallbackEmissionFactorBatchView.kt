package com.acme.transport.emissions.emissionfactor.factorproposal

/** How screens and exports show a fallback emission factor batch. */
data class FallbackEmissionFactorBatchView(
    var code: String,
    var name: String,
    var endsAt: String,
    var contactEmail: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toFallbackEmissionFactorBatchView(item: FallbackEmissionFactorBatch): FallbackEmissionFactorBatchView {
    return FallbackEmissionFactorBatchView(
        code = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        contactEmail = item.contactEmail,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEmissionFactorBatchViews(items: MutableList<FallbackEmissionFactorBatch>): MutableList<FallbackEmissionFactorBatchView> {
    val views = mutableListOf<FallbackEmissionFactorBatchView>()
    for (item in items) {
        views.add(toFallbackEmissionFactorBatchView(item))
    }
    return views
}

fun sumFallbackEmissionFactorBatchView(view: FallbackEmissionFactorBatchView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEndsAt.toDouble() + parsedCeiling
}
