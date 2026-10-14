package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default parcel milestone. */
data class DefaultParcelMilestoneView(
    var code: String,
    var label: String,
    var sourceText: String,
    var contactEmailText: String,
    var weight: String,
    var retries: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDefaultParcelMilestoneView(item: DefaultParcelMilestone): DefaultParcelMilestoneView {
    return DefaultParcelMilestoneView(
        code = item.code,
        label = truncate(item.label, 80),
        sourceText = item.source,
        contactEmailText = item.contactEmail,
        weight = "%.2f".format(item.weight),
        retries = item.retries.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultParcelMilestoneViews(items: MutableList<DefaultParcelMilestone>): MutableList<DefaultParcelMilestoneView> {
    val views = mutableListOf<DefaultParcelMilestoneView>()
    for (item in items) {
        views.add(toDefaultParcelMilestoneView(item))
    }
    return views
}

fun sumDefaultParcelMilestoneView(view: DefaultParcelMilestoneView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedWeight + parsedRetries.toDouble() + parsedExpiresAt.toDouble()
}
