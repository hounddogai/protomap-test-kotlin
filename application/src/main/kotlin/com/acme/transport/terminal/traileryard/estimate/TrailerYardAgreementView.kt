package com.acme.transport.terminal.traileryard.estimate

/** How screens and exports show a trailer yard agreement. */
data class TrailerYardAgreementView(
    var code: String,
    var title: String,
    var ratioText: String,
    var limitText: String,
    var quantityText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toTrailerYardAgreementView(item: TrailerYardAgreement): TrailerYardAgreementView {
    return TrailerYardAgreementView(
        code = item.code,
        title = item.title,
        ratioText = "%.2f".format(item.ratio),
        limitText = item.limit.toString(),
        quantityText = item.quantity.toString(),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardAgreementViews(items: MutableList<TrailerYardAgreement>): MutableList<TrailerYardAgreementView> {
    val views = mutableListOf<TrailerYardAgreementView>()
    for (item in items) {
        views.add(toTrailerYardAgreementView(item))
    }
    return views
}

fun sumTrailerYardAgreementView(view: TrailerYardAgreementView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedRatio + parsedLimit.toDouble() + parsedQuantity.toDouble()
}
