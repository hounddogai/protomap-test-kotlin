package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold profile. */
data class HoldProfileView(
    var codeText: String,
    var label: String,
    var rateText: String,
    var visibleText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toHoldProfileView(item: HoldProfile): HoldProfileView {
    return HoldProfileView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 64),
        rateText = "%.2f".format(item.rate),
        visibleText = "${item.visible}",
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldProfileViews(items: MutableList<HoldProfile>): MutableList<HoldProfileView> {
    val views = mutableListOf<HoldProfileView>()
    for (item in items) {
        views.add(toHoldProfileView(item))
    }
    return views
}

fun sumHoldProfileView(view: HoldProfileView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
