package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global qualification digest. */
data class GlobalQualificationDigestView(
    var code: String,
    var name: String,
    var odometerText: String,
    var expiresAt: String,
    var countryText: String,
    var updatedAt: String,
    var graceDays: String,
    var quantityText: String,
    var currency: String,
    var statusLabel: String,
)

fun toGlobalQualificationDigestView(item: GlobalQualificationDigest): GlobalQualificationDigestView {
    return GlobalQualificationDigestView(
        code = item.code,
        name = truncate(item.name, 40),
        odometerText = item.odometer.toString(),
        expiresAt = item.expiresAt.toString(),
        countryText = truncate(item.country, 64),
        updatedAt = item.updatedAt.toString(),
        graceDays = item.graceDays.toString(),
        quantityText = item.quantity.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalQualificationDigestViews(items: MutableList<GlobalQualificationDigest>): MutableList<GlobalQualificationDigestView> {
    val views = mutableListOf<GlobalQualificationDigestView>()
    for (item in items) {
        views.add(toGlobalQualificationDigestView(item))
    }
    return views
}

fun sumGlobalQualificationDigestView(view: GlobalQualificationDigestView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedOdometer.toDouble() + parsedExpiresAt.toDouble() + parsedUpdatedAt.toDouble()
}
