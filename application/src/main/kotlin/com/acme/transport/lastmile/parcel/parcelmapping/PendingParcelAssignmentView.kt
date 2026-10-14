package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending parcel assignment. */
data class PendingParcelAssignmentView(
    var code: String,
    var titleText: String,
    var label: String,
    var rate: String,
    var country: String,
    var regionText: String,
    var descriptionText: String,
    var expiresAt: String,
    var checksum: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toPendingParcelAssignmentView(item: PendingParcelAssignment): PendingParcelAssignmentView {
    return PendingParcelAssignmentView(
        code = item.code,
        titleText = item.title,
        label = truncate(item.label, 80),
        rate = "%.2f".format(item.rate),
        country = truncate(item.country, 40),
        regionText = item.region,
        descriptionText = item.description,
        expiresAt = item.expiresAt.toString(),
        checksum = truncate(item.checksum, 40),
        contactEmailText = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingParcelAssignmentViews(items: MutableList<PendingParcelAssignment>): MutableList<PendingParcelAssignmentView> {
    val views = mutableListOf<PendingParcelAssignmentView>()
    for (item in items) {
        views.add(toPendingParcelAssignmentView(item))
    }
    return views
}

fun sumPendingParcelAssignmentView(view: PendingParcelAssignmentView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedRate + parsedExpiresAt.toDouble()
}
