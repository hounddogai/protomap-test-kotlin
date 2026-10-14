package com.acme.transport.linehaul.tender.tenderrollup

/** How screens and exports show a strict tender rollup. */
data class StrictTenderRollupView(
    var codeText: String,
    var labelText: String,
    var updatedAt: String,
    var name: String,
    var expiresAtText: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictTenderRollupView(item: StrictTenderRollup): StrictTenderRollupView {
    return StrictTenderRollupView(
        codeText = item.code,
        labelText = item.label,
        updatedAt = item.updatedAt.toString(),
        name = item.name,
        expiresAtText = item.expiresAt.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTenderRollupViews(items: MutableList<StrictTenderRollup>): MutableList<StrictTenderRollupView> {
    val views = mutableListOf<StrictTenderRollupView>()
    for (item in items) {
        views.add(toStrictTenderRollupView(item))
    }
    return views
}

fun sumStrictTenderRollupView(view: StrictTenderRollupView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedAmount
}
