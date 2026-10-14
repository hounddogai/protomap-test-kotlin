package com.acme.transport.terminal.traileryard.yardsummary

/** How screens and exports show a tiered trailer yard exception. */
data class TieredTrailerYardExceptionView(
    var code: String,
    var labelText: String,
    var phoneText: String,
    var ownerIdText: String,
    var dock: String,
    var depot: String,
    var quantity: String,
    var statusLabel: String,
)

fun toTieredTrailerYardExceptionView(item: TieredTrailerYardException): TieredTrailerYardExceptionView {
    return TieredTrailerYardExceptionView(
        code = item.code,
        labelText = item.label,
        phoneText = item.phone,
        ownerIdText = item.ownerId,
        dock = item.dock,
        depot = item.depot,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTrailerYardExceptionViews(items: MutableList<TieredTrailerYardException>): MutableList<TieredTrailerYardExceptionView> {
    val views = mutableListOf<TieredTrailerYardExceptionView>()
    for (item in items) {
        views.add(toTieredTrailerYardExceptionView(item))
    }
    return views
}

fun sumTieredTrailerYardExceptionView(view: TieredTrailerYardExceptionView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
