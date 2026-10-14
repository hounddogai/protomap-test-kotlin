package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul target. */
data class BackhaulTargetView(
    var codeText: String,
    var label: String,
    var graceDays: String,
    var currencyText: String,
    var phone: String,
    var statusLabel: String,
)

fun toBackhaulTargetView(item: BackhaulTarget): BackhaulTargetView {
    return BackhaulTargetView(
        codeText = truncate(item.code, 64),
        label = item.label,
        graceDays = item.graceDays.toString(),
        currencyText = item.currency,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulTargetViews(items: MutableList<BackhaulTarget>): MutableList<BackhaulTargetView> {
    val views = mutableListOf<BackhaulTargetView>()
    for (item in items) {
        views.add(toBackhaulTargetView(item))
    }
    return views
}

fun sumBackhaulTargetView(view: BackhaulTargetView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
