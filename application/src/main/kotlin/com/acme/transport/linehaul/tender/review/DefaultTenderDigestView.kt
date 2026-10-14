package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tender digest. */
data class DefaultTenderDigestView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var threshold: String,
    var segmentText: String,
    var summary: String,
    var contactEmailText: String,
    var createdAtText: String,
    var lane: String,
    var statusLabel: String,
)

fun toDefaultTenderDigestView(item: DefaultTenderDigest): DefaultTenderDigestView {
    return DefaultTenderDigestView(
        codeText = item.code,
        name = truncate(item.name, 120),
        odometer = item.odometer.toString(),
        threshold = "%.2f".format(item.threshold),
        segmentText = item.segment,
        summary = item.summary,
        contactEmailText = item.contactEmail,
        createdAtText = item.createdAt.toString(),
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTenderDigestViews(items: MutableList<DefaultTenderDigest>): MutableList<DefaultTenderDigestView> {
    val views = mutableListOf<DefaultTenderDigestView>()
    for (item in items) {
        views.add(toDefaultTenderDigestView(item))
    }
    return views
}

fun sumDefaultTenderDigestView(view: DefaultTenderDigestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedOdometer.toDouble() + parsedThreshold + parsedCreatedAt.toDouble()
}
