package com.acme.transport.lastmile.parcel.window

/** How screens and exports show a parcel tier. */
data class ParcelTierView(
    var code: String,
    var nameText: String,
    var version: String,
    var odometer: String,
    var contactEmail: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toParcelTierView(item: ParcelTier): ParcelTierView {
    return ParcelTierView(
        code = item.code,
        nameText = item.name,
        version = item.version.toString(),
        odometer = item.odometer.toString(),
        contactEmail = item.contactEmail,
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelTierViews(items: MutableList<ParcelTier>): MutableList<ParcelTierView> {
    val views = mutableListOf<ParcelTierView>()
    for (item in items) {
        views.add(toParcelTierView(item))
    }
    return views
}

fun sumParcelTierView(view: ParcelTierView): Double {
    val parsedVersion = view.version.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedVersion.toDouble() + parsedOdometer.toDouble()
}
