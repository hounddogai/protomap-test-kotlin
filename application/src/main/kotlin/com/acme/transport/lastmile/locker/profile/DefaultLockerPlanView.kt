package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default locker plan. */
data class DefaultLockerPlanView(
    var code: String,
    var titleText: String,
    var ownerId: String,
    var weight: String,
    var summary: String,
    var expiresAtText: String,
    var contactEmail: String,
    var timezone: String,
    var maximumText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDefaultLockerPlanView(item: DefaultLockerPlan): DefaultLockerPlanView {
    return DefaultLockerPlanView(
        code = item.code,
        titleText = item.title,
        ownerId = truncate(item.ownerId, 40),
        weight = "%.2f".format(item.weight),
        summary = item.summary,
        expiresAtText = item.expiresAt.toString(),
        contactEmail = item.contactEmail,
        timezone = item.timezone,
        maximumText = "%.2f".format(item.maximum),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultLockerPlanViews(items: MutableList<DefaultLockerPlan>): MutableList<DefaultLockerPlanView> {
    val views = mutableListOf<DefaultLockerPlanView>()
    for (item in items) {
        views.add(toDefaultLockerPlanView(item))
    }
    return views
}

fun sumDefaultLockerPlanView(view: DefaultLockerPlanView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedWeight + parsedExpiresAt.toDouble() + parsedMaximum
}
