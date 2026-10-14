package com.acme.transport.lastmile.parcel.policy

/** How screens and exports show a weighted parcel template. */
data class WeightedParcelTemplateView(
    var code: String,
    var label: String,
    var limitText: String,
    var activeText: String,
    var scoreText: String,
    var discount: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toWeightedParcelTemplateView(item: WeightedParcelTemplate): WeightedParcelTemplateView {
    return WeightedParcelTemplateView(
        code = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        activeText = "${item.active}",
        scoreText = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedParcelTemplateViews(items: MutableList<WeightedParcelTemplate>): MutableList<WeightedParcelTemplateView> {
    val views = mutableListOf<WeightedParcelTemplateView>()
    for (item in items) {
        views.add(toWeightedParcelTemplateView(item))
    }
    return views
}

fun sumWeightedParcelTemplateView(view: WeightedParcelTemplateView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedLimit.toDouble() + parsedScore + parsedDiscount
}
