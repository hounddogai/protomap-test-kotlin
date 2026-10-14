package com.acme.transport.lastmile.parcel.parcelbaseline

/** How screens and exports show a capped parcel entry. */
data class CappedParcelEntryView(
    var code: String,
    var name: String,
    var depot: String,
    var approvedText: String,
    var retries: String,
    var pallets: String,
    var pinned: String,
    var minimum: String,
    var statusLabel: String,
)

fun toCappedParcelEntryView(item: CappedParcelEntry): CappedParcelEntryView {
    return CappedParcelEntryView(
        code = item.code,
        name = item.name,
        depot = item.depot,
        approvedText = "${item.approved}",
        retries = item.retries.toString(),
        pallets = item.pallets.toString(),
        pinned = "${item.pinned}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedParcelEntryViews(items: MutableList<CappedParcelEntry>): MutableList<CappedParcelEntryView> {
    val views = mutableListOf<CappedParcelEntryView>()
    for (item in items) {
        views.add(toCappedParcelEntryView(item))
    }
    return views
}

fun sumCappedParcelEntryView(view: CappedParcelEntryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRetries.toDouble() + parsedPallets.toDouble() + parsedMinimum
}
