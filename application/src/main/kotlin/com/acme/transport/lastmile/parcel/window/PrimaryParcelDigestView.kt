package com.acme.transport.lastmile.parcel.window

/** How screens and exports show a primary parcel digest. */
data class PrimaryParcelDigestView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var archived: String,
    var quantity: String,
    var currency: String,
    var updatedAt: String,
    var dockText: String,
    var statusLabel: String,
)

fun toPrimaryParcelDigestView(item: PrimaryParcelDigest): PrimaryParcelDigestView {
    return PrimaryParcelDigestView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        archived = "${item.archived}",
        quantity = item.quantity.toString(),
        currency = item.currency,
        updatedAt = item.updatedAt.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryParcelDigestViews(items: MutableList<PrimaryParcelDigest>): MutableList<PrimaryParcelDigestView> {
    val views = mutableListOf<PrimaryParcelDigestView>()
    for (item in items) {
        views.add(toPrimaryParcelDigestView(item))
    }
    return views
}

fun sumPrimaryParcelDigestView(view: PrimaryParcelDigestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedQuantity.toDouble() + parsedUpdatedAt.toDouble()
}
