package com.acme.transport.lastmile.parcel.metric

/** How screens and exports show a parcel exception. */
data class ParcelExceptionView(
    var codeText: String,
    var labelText: String,
    var pallets: String,
    var loadKgText: String,
    var shiftCode: String,
    var expiresAt: String,
    var dock: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toParcelExceptionView(item: ParcelException): ParcelExceptionView {
    return ParcelExceptionView(
        codeText = item.code,
        labelText = item.label,
        pallets = item.pallets.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        shiftCode = item.shiftCode,
        expiresAt = item.expiresAt.toString(),
        dock = item.dock,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelExceptionViews(items: MutableList<ParcelException>): MutableList<ParcelExceptionView> {
    val views = mutableListOf<ParcelExceptionView>()
    for (item in items) {
        views.add(toParcelExceptionView(item))
    }
    return views
}

fun sumParcelExceptionView(view: ParcelExceptionView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedPallets.toDouble() + parsedLoadKg + parsedExpiresAt.toDouble()
}
