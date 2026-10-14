package com.acme.transport.crewing.availability.availabilitydigest

/** How screens and exports show a legacy availability digest. */
data class LegacyAvailabilityDigestView(
    var code: String,
    var name: String,
    var carrier: String,
    var contactEmail: String,
    var sequenceText: String,
    var depot: String,
    var driverId: String,
    var titleText: String,
    var archived: String,
    var weightText: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toLegacyAvailabilityDigestView(item: LegacyAvailabilityDigest): LegacyAvailabilityDigestView {
    return LegacyAvailabilityDigestView(
        code = item.code,
        name = item.name,
        carrier = item.carrier,
        contactEmail = item.contactEmail,
        sequenceText = item.sequence.toString(),
        depot = item.depot,
        driverId = item.driverId,
        titleText = item.title,
        archived = "${item.archived}",
        weightText = "%.2f".format(item.weight),
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAvailabilityDigestViews(items: MutableList<LegacyAvailabilityDigest>): MutableList<LegacyAvailabilityDigestView> {
    val views = mutableListOf<LegacyAvailabilityDigestView>()
    for (item in items) {
        views.add(toLegacyAvailabilityDigestView(item))
    }
    return views
}

fun sumLegacyAvailabilityDigestView(view: LegacyAvailabilityDigestView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedSequence.toDouble() + parsedWeight
}
