package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary redelivery preference. */
data class PrimaryRedeliveryPreferenceView(
    var code: String,
    var nameText: String,
    var endsAt: String,
    var visible: String,
    var retries: String,
    var checksum: String,
    var phoneText: String,
    var pallets: String,
    var archived: String,
    var statusLabel: String,
)

fun toPrimaryRedeliveryPreferenceView(item: PrimaryRedeliveryPreference): PrimaryRedeliveryPreferenceView {
    return PrimaryRedeliveryPreferenceView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        endsAt = item.endsAt.toString(),
        visible = "${item.visible}",
        retries = item.retries.toString(),
        checksum = item.checksum,
        phoneText = item.phone,
        pallets = item.pallets.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRedeliveryPreferenceViews(items: MutableList<PrimaryRedeliveryPreference>): MutableList<PrimaryRedeliveryPreferenceView> {
    val views = mutableListOf<PrimaryRedeliveryPreferenceView>()
    for (item in items) {
        views.add(toPrimaryRedeliveryPreferenceView(item))
    }
    return views
}

fun sumPrimaryRedeliveryPreferenceView(view: PrimaryRedeliveryPreferenceView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble() + parsedPallets.toDouble()
}
