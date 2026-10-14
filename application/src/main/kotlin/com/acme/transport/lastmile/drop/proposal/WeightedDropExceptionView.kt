package com.acme.transport.lastmile.drop.proposal

/** How screens and exports show a weighted drop exception. */
data class WeightedDropExceptionView(
    var code: String,
    var titleText: String,
    var amount: String,
    var activeText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toWeightedDropExceptionView(item: WeightedDropException): WeightedDropExceptionView {
    return WeightedDropExceptionView(
        code = item.code,
        titleText = item.title,
        amount = "%.2f".format(item.amount),
        activeText = "${item.active}",
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDropExceptionViews(items: MutableList<WeightedDropException>): MutableList<WeightedDropExceptionView> {
    val views = mutableListOf<WeightedDropExceptionView>()
    for (item in items) {
        views.add(toWeightedDropExceptionView(item))
    }
    return views
}

fun sumWeightedDropExceptionView(view: WeightedDropExceptionView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
