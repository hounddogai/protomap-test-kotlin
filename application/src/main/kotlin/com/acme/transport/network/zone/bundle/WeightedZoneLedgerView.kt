package com.acme.transport.network.zone.bundle

/** How screens and exports show a weighted zone ledger. */
data class WeightedZoneLedgerView(
    var code: String,
    var title: String,
    var sequenceText: String,
    var createdAt: String,
    var fuelLitersText: String,
    var contactEmail: String,
    var locale: String,
    var quantity: String,
    var currency: String,
    var shiftCodeText: String,
    var label: String,
    var statusLabel: String,
)

fun toWeightedZoneLedgerView(item: WeightedZoneLedger): WeightedZoneLedgerView {
    return WeightedZoneLedgerView(
        code = item.code,
        title = item.title,
        sequenceText = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        contactEmail = item.contactEmail,
        locale = item.locale,
        quantity = item.quantity.toString(),
        currency = item.currency,
        shiftCodeText = item.shiftCode,
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedZoneLedgerViews(items: MutableList<WeightedZoneLedger>): MutableList<WeightedZoneLedgerView> {
    val views = mutableListOf<WeightedZoneLedgerView>()
    for (item in items) {
        views.add(toWeightedZoneLedgerView(item))
    }
    return views
}

fun sumWeightedZoneLedgerView(view: WeightedZoneLedgerView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedFuelLiters
}
