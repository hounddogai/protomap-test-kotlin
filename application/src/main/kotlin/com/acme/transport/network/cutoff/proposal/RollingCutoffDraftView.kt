package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling cutoff draft. */
data class RollingCutoffDraftView(
    var codeText: String,
    var name: String,
    var descriptionText: String,
    var visibleText: String,
    var contactEmailText: String,
    var revision: String,
    var locale: String,
    var threshold: String,
    var region: String,
    var discountText: String,
    var statusLabel: String,
)

fun toRollingCutoffDraftView(item: RollingCutoffDraft): RollingCutoffDraftView {
    return RollingCutoffDraftView(
        codeText = item.code,
        name = item.name,
        descriptionText = truncate(item.description, 40),
        visibleText = "${item.visible}",
        contactEmailText = truncate(item.contactEmail, 64),
        revision = item.revision.toString(),
        locale = truncate(item.locale, 80),
        threshold = "%.2f".format(item.threshold),
        region = truncate(item.region, 80),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCutoffDraftViews(items: MutableList<RollingCutoffDraft>): MutableList<RollingCutoffDraftView> {
    val views = mutableListOf<RollingCutoffDraftView>()
    for (item in items) {
        views.add(toRollingCutoffDraftView(item))
    }
    return views
}

fun sumRollingCutoffDraftView(view: RollingCutoffDraftView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRevision.toDouble() + parsedThreshold + parsedDiscount
}
