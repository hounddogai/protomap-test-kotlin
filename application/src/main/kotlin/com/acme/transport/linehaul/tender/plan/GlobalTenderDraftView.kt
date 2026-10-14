package com.acme.transport.linehaul.tender.plan

/** How screens and exports show a global tender draft. */
data class GlobalTenderDraftView(
    var code: String,
    var nameText: String,
    var title: String,
    var ratio: String,
    var quantity: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalTenderDraftView(item: GlobalTenderDraft): GlobalTenderDraftView {
    return GlobalTenderDraftView(
        code = item.code,
        nameText = item.name,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        quantity = item.quantity.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderDraftViews(items: MutableList<GlobalTenderDraft>): MutableList<GlobalTenderDraftView> {
    val views = mutableListOf<GlobalTenderDraftView>()
    for (item in items) {
        views.add(toGlobalTenderDraftView(item))
    }
    return views
}

fun sumGlobalTenderDraftView(view: GlobalTenderDraftView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRatio + parsedQuantity.toDouble()
}
