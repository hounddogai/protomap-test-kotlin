package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return manifest. */
data class LeaseReturnManifestView(
    var code: String,
    var labelText: String,
    var fuelLitersText: String,
    var phone: String,
    var countryText: String,
    var rateText: String,
    var endsAt: String,
    var carrier: String,
    var statusLabel: String,
)

fun toLeaseReturnManifestView(item: LeaseReturnManifest): LeaseReturnManifestView {
    return LeaseReturnManifestView(
        code = item.code,
        labelText = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        countryText = truncate(item.country, 40),
        rateText = "%.2f".format(item.rate),
        endsAt = item.endsAt.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnManifestViews(items: MutableList<LeaseReturnManifest>): MutableList<LeaseReturnManifestView> {
    val views = mutableListOf<LeaseReturnManifestView>()
    for (item in items) {
        views.add(toLeaseReturnManifestView(item))
    }
    return views
}

fun sumLeaseReturnManifestView(view: LeaseReturnManifestView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedFuelLiters + parsedRate + parsedEndsAt.toDouble()
}
