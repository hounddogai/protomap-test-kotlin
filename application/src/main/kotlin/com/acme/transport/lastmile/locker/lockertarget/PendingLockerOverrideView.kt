package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending locker override. */
data class PendingLockerOverrideView(
    var codeText: String,
    var title: String,
    var currencyText: String,
    var region: String,
    var shiftCode: String,
    var carrier: String,
    var externalRef: String,
    var reviewerText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPendingLockerOverrideView(item: PendingLockerOverride): PendingLockerOverrideView {
    return PendingLockerOverrideView(
        codeText = item.code,
        title = item.title,
        currencyText = item.currency,
        region = item.region,
        shiftCode = item.shiftCode,
        carrier = truncate(item.carrier, 64),
        externalRef = item.externalRef,
        reviewerText = truncate(item.reviewer, 80),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLockerOverrideViews(items: MutableList<PendingLockerOverride>): MutableList<PendingLockerOverrideView> {
    val views = mutableListOf<PendingLockerOverrideView>()
    for (item in items) {
        views.add(toPendingLockerOverrideView(item))
    }
    return views
}

fun sumPendingLockerOverrideView(view: PendingLockerOverrideView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
