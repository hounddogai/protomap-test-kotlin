package com.acme.transport.freightpay.accessorial.manifest

/** How screens and exports show a accessorial bundle. */
data class AccessorialBundleView(
    var code: String,
    var labelText: String,
    var margin: String,
    var discount: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toAccessorialBundleView(item: AccessorialBundle): AccessorialBundleView {
    return AccessorialBundleView(
        code = item.code,
        labelText = item.label,
        margin = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialBundleViews(items: MutableList<AccessorialBundle>): MutableList<AccessorialBundleView> {
    val views = mutableListOf<AccessorialBundleView>()
    for (item in items) {
        views.add(toAccessorialBundleView(item))
    }
    return views
}

fun sumAccessorialBundleView(view: AccessorialBundleView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMargin + parsedDiscount + parsedLoadKg
}
