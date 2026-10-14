package com.acme.transport.emissions.evcharge.assignment

/** How screens and exports show a fallback ev charge group. */
data class FallbackEvChargeGroupView(
    var code: String,
    var titleText: String,
    var category: String,
    var locale: String,
    var approved: String,
    var marginText: String,
    var statusLabel: String,
)

fun toFallbackEvChargeGroupView(item: FallbackEvChargeGroup): FallbackEvChargeGroupView {
    return FallbackEvChargeGroupView(
        code = item.code,
        titleText = item.title,
        category = item.category,
        locale = item.locale,
        approved = "${item.approved}",
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEvChargeGroupViews(items: MutableList<FallbackEvChargeGroup>): MutableList<FallbackEvChargeGroupView> {
    val views = mutableListOf<FallbackEvChargeGroupView>()
    for (item in items) {
        views.add(toFallbackEvChargeGroupView(item))
    }
    return views
}

fun sumFallbackEvChargeGroupView(view: FallbackEvChargeGroupView): Double {
    val parsedMargin = view.marginText.toDouble()
    return parsedMargin
}
