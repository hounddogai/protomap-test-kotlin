package com.acme.transport.lastmile.parcel.parcelreservation

/** How screens and exports show a manual parcel window. */
data class ManualParcelWindowView(
    var code: String,
    var name: String,
    var sourceText: String,
    var amount: String,
    var channel: String,
    var updatedAtText: String,
    var createdAt: String,
    var approved: String,
    var windowDays: String,
    var locale: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toManualParcelWindowView(item: ManualParcelWindow): ManualParcelWindowView {
    return ManualParcelWindowView(
        code = item.code,
        name = item.name,
        sourceText = item.source,
        amount = "%.2f".format(item.amount),
        channel = item.channel,
        updatedAtText = item.updatedAt.toString(),
        createdAt = item.createdAt.toString(),
        approved = "${item.approved}",
        windowDays = item.windowDays.toString(),
        locale = item.locale,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualParcelWindowViews(items: MutableList<ManualParcelWindow>): MutableList<ManualParcelWindowView> {
    val views = mutableListOf<ManualParcelWindowView>()
    for (item in items) {
        views.add(toManualParcelWindowView(item))
    }
    return views
}

fun sumManualParcelWindowView(view: ManualParcelWindowView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedAmount + parsedUpdatedAt.toDouble() + parsedCreatedAt.toDouble()
}
