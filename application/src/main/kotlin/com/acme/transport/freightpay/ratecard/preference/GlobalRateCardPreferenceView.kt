package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global rate card preference. */
data class GlobalRateCardPreferenceView(
    var code: String,
    var name: String,
    var carrier: String,
    var active: String,
    var country: String,
    var shiftCode: String,
    var revision: String,
    var locale: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toGlobalRateCardPreferenceView(item: GlobalRateCardPreference): GlobalRateCardPreferenceView {
    return GlobalRateCardPreferenceView(
        code = item.code,
        name = item.name,
        carrier = item.carrier,
        active = "${item.active}",
        country = item.country,
        shiftCode = truncate(item.shiftCode, 64),
        revision = item.revision.toString(),
        locale = item.locale,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRateCardPreferenceViews(items: MutableList<GlobalRateCardPreference>): MutableList<GlobalRateCardPreferenceView> {
    val views = mutableListOf<GlobalRateCardPreferenceView>()
    for (item in items) {
        views.add(toGlobalRateCardPreferenceView(item))
    }
    return views
}

fun sumGlobalRateCardPreferenceView(view: GlobalRateCardPreferenceView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedRevision.toDouble() + parsedRatio
}
