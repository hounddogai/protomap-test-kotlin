package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial baseline. */
data class AccessorialBaselineView(
    var codeText: String,
    var titleText: String,
    var countryText: String,
    var nameText: String,
    var depot: String,
    var localeText: String,
    var odometer: String,
    var revisionText: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toAccessorialBaselineView(item: AccessorialBaseline): AccessorialBaselineView {
    return AccessorialBaselineView(
        codeText = item.code,
        titleText = item.title,
        countryText = item.country,
        nameText = item.name,
        depot = item.depot,
        localeText = truncate(item.locale, 120),
        odometer = item.odometer.toString(),
        revisionText = item.revision.toString(),
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialBaselineViews(items: MutableList<AccessorialBaseline>): MutableList<AccessorialBaselineView> {
    val views = mutableListOf<AccessorialBaselineView>()
    for (item in items) {
        views.add(toAccessorialBaselineView(item))
    }
    return views
}

fun sumAccessorialBaselineView(view: AccessorialBaselineView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedOdometer.toDouble() + parsedRevision.toDouble()
}
