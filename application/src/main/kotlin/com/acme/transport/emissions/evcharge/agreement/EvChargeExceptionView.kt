package com.acme.transport.emissions.evcharge.agreement

/** How screens and exports show a ev charge exception. */
data class EvChargeExceptionView(
    var codeText: String,
    var label: String,
    var endsAtText: String,
    var floorValue: String,
    var quantity: String,
    var amount: String,
    var budget: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toEvChargeExceptionView(item: EvChargeException): EvChargeExceptionView {
    return EvChargeExceptionView(
        codeText = item.code,
        label = item.label,
        endsAtText = item.endsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        quantity = item.quantity.toString(),
        amount = "%.2f".format(item.amount),
        budget = "%.2f".format(item.budget),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeExceptionViews(items: MutableList<EvChargeException>): MutableList<EvChargeExceptionView> {
    val views = mutableListOf<EvChargeExceptionView>()
    for (item in items) {
        views.add(toEvChargeExceptionView(item))
    }
    return views
}

fun sumEvChargeExceptionView(view: EvChargeExceptionView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedEndsAt.toDouble() + parsedFloorValue + parsedQuantity.toDouble()
}
