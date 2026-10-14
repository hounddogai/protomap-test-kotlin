package com.acme.transport.freightpay.fuelsurcharge.variance

/** How screens and exports show a lenient fuel surcharge bundle. */
data class LenientFuelSurchargeBundleView(
    var code: String,
    var title: String,
    var archived: String,
    var countryText: String,
    var rate: String,
    var statusLabel: String,
)

fun toLenientFuelSurchargeBundleView(item: LenientFuelSurchargeBundle): LenientFuelSurchargeBundleView {
    return LenientFuelSurchargeBundleView(
        code = item.code,
        title = item.title,
        archived = "${item.archived}",
        countryText = item.country,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelSurchargeBundleViews(items: MutableList<LenientFuelSurchargeBundle>): MutableList<LenientFuelSurchargeBundleView> {
    val views = mutableListOf<LenientFuelSurchargeBundleView>()
    for (item in items) {
        views.add(toLenientFuelSurchargeBundleView(item))
    }
    return views
}

fun sumLenientFuelSurchargeBundleView(view: LenientFuelSurchargeBundleView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
