package com.acme.transport.fleetops.tire.exception

/** How screens and exports show a capped tire exception. */
data class CappedTireExceptionView(
    var code: String,
    var label: String,
    var description: String,
    var discount: String,
    var cost: String,
    var limitText: String,
    var countryText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toCappedTireExceptionView(item: CappedTireException): CappedTireExceptionView {
    return CappedTireExceptionView(
        code = item.code,
        label = item.label,
        description = item.description,
        discount = "%.2f".format(item.discount),
        cost = "%.2f".format(item.cost),
        limitText = item.limit.toString(),
        countryText = item.country,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTireExceptionViews(items: MutableList<CappedTireException>): MutableList<CappedTireExceptionView> {
    val views = mutableListOf<CappedTireExceptionView>()
    for (item in items) {
        views.add(toCappedTireExceptionView(item))
    }
    return views
}

fun sumCappedTireExceptionView(view: CappedTireExceptionView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedDiscount + parsedCost + parsedLimit.toDouble()
}
