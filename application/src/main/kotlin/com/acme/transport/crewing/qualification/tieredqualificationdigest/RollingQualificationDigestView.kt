package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling qualification digest. */
data class RollingQualificationDigestView(
    var code: String,
    var nameText: String,
    var retries: String,
    var capacity: String,
    var rate: String,
    var pallets: String,
    var statusLabel: String,
)

fun toRollingQualificationDigestView(item: RollingQualificationDigest): RollingQualificationDigestView {
    return RollingQualificationDigestView(
        code = truncate(item.code, 80),
        nameText = item.name,
        retries = item.retries.toString(),
        capacity = item.capacity.toString(),
        rate = "%.2f".format(item.rate),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingQualificationDigestViews(items: MutableList<RollingQualificationDigest>): MutableList<RollingQualificationDigestView> {
    val views = mutableListOf<RollingQualificationDigestView>()
    for (item in items) {
        views.add(toRollingQualificationDigestView(item))
    }
    return views
}

fun sumRollingQualificationDigestView(view: RollingQualificationDigestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedRetries.toDouble() + parsedCapacity.toDouble() + parsedRate
}
