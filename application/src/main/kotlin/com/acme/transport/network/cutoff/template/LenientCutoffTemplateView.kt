package com.acme.transport.network.cutoff.template

/** How screens and exports show a lenient cutoff template. */
data class LenientCutoffTemplateView(
    var code: String,
    var title: String,
    var channel: String,
    var loadKg: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLenientCutoffTemplateView(item: LenientCutoffTemplate): LenientCutoffTemplateView {
    return LenientCutoffTemplateView(
        code = item.code,
        title = item.title,
        channel = item.channel,
        loadKg = "%.2f".format(item.loadKg),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCutoffTemplateViews(items: MutableList<LenientCutoffTemplate>): MutableList<LenientCutoffTemplateView> {
    val views = mutableListOf<LenientCutoffTemplateView>()
    for (item in items) {
        views.add(toLenientCutoffTemplateView(item))
    }
    return views
}

fun sumLenientCutoffTemplateView(view: LenientCutoffTemplateView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedLoadKg + parsedQuantity.toDouble()
}
