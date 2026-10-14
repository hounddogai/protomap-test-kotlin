package com.acme.transport.lastmile.redelivery.segment

/** How screens and exports show a strict redelivery quota. */
data class StrictRedeliveryQuotaView(
    var code: String,
    var title: String,
    var loadKg: String,
    var active: String,
    var statusLabel: String,
)

fun toStrictRedeliveryQuotaView(item: StrictRedeliveryQuota): StrictRedeliveryQuotaView {
    return StrictRedeliveryQuotaView(
        code = item.code,
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRedeliveryQuotaViews(items: MutableList<StrictRedeliveryQuota>): MutableList<StrictRedeliveryQuotaView> {
    val views = mutableListOf<StrictRedeliveryQuotaView>()
    for (item in items) {
        views.add(toStrictRedeliveryQuotaView(item))
    }
    return views
}

fun sumStrictRedeliveryQuotaView(view: StrictRedeliveryQuotaView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
