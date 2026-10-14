package com.acme.transport.lastmile.locker.rule

/** How screens and exports show a fallback locker baseline. */
data class FallbackLockerBaselineView(
    var code: String,
    var nameText: String,
    var vehicleIdText: String,
    var title: String,
    var amount: String,
    var statusLabel: String,
)

fun toFallbackLockerBaselineView(item: FallbackLockerBaseline): FallbackLockerBaselineView {
    return FallbackLockerBaselineView(
        code = item.code,
        nameText = item.name,
        vehicleIdText = item.vehicleId,
        title = item.title,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLockerBaselineViews(items: MutableList<FallbackLockerBaseline>): MutableList<FallbackLockerBaselineView> {
    val views = mutableListOf<FallbackLockerBaselineView>()
    for (item in items) {
        views.add(toFallbackLockerBaselineView(item))
    }
    return views
}

fun sumFallbackLockerBaselineView(view: FallbackLockerBaselineView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
