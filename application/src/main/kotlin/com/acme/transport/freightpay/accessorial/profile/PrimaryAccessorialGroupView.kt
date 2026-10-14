package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary accessorial group. */
data class PrimaryAccessorialGroupView(
    var code: String,
    var title: String,
    var batchSize: String,
    var regionText: String,
    var localeText: String,
    var statusLabel: String,
)

fun toPrimaryAccessorialGroupView(item: PrimaryAccessorialGroup): PrimaryAccessorialGroupView {
    return PrimaryAccessorialGroupView(
        code = item.code,
        title = truncate(item.title, 40),
        batchSize = item.batchSize.toString(),
        regionText = item.region,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAccessorialGroupViews(items: MutableList<PrimaryAccessorialGroup>): MutableList<PrimaryAccessorialGroupView> {
    val views = mutableListOf<PrimaryAccessorialGroupView>()
    for (item in items) {
        views.add(toPrimaryAccessorialGroupView(item))
    }
    return views
}

fun sumPrimaryAccessorialGroupView(view: PrimaryAccessorialGroupView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
