package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dispute profile. */
data class GlobalDisputeProfileView(
    var code: String,
    var title: String,
    var rate: String,
    var active: String,
    var externalRef: String,
    var region: String,
    var shiftCode: String,
    var localeText: String,
    var minimum: String,
    var limit: String,
    var statusLabel: String,
)

fun toGlobalDisputeProfileView(item: GlobalDisputeProfile): GlobalDisputeProfileView {
    return GlobalDisputeProfileView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        active = "${item.active}",
        externalRef = truncate(item.externalRef, 80),
        region = item.region,
        shiftCode = item.shiftCode,
        localeText = item.locale,
        minimum = "%.2f".format(item.minimum),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDisputeProfileViews(items: MutableList<GlobalDisputeProfile>): MutableList<GlobalDisputeProfileView> {
    val views = mutableListOf<GlobalDisputeProfileView>()
    for (item in items) {
        views.add(toGlobalDisputeProfileView(item))
    }
    return views
}

fun sumGlobalDisputeProfileView(view: GlobalDisputeProfileView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedRate + parsedMinimum + parsedLimit.toDouble()
}
