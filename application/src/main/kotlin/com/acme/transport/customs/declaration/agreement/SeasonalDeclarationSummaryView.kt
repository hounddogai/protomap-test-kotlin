package com.acme.transport.customs.declaration.agreement

/** How screens and exports show a seasonal declaration summary. */
data class SeasonalDeclarationSummaryView(
    var codeText: String,
    var title: String,
    var discount: String,
    var cost: String,
    var endsAt: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toSeasonalDeclarationSummaryView(item: SeasonalDeclarationSummary): SeasonalDeclarationSummaryView {
    return SeasonalDeclarationSummaryView(
        codeText = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        cost = "%.2f".format(item.cost),
        endsAt = item.endsAt.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDeclarationSummaryViews(items: MutableList<SeasonalDeclarationSummary>): MutableList<SeasonalDeclarationSummaryView> {
    val views = mutableListOf<SeasonalDeclarationSummaryView>()
    for (item in items) {
        views.add(toSeasonalDeclarationSummaryView(item))
    }
    return views
}

fun sumSeasonalDeclarationSummaryView(view: SeasonalDeclarationSummaryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedCost + parsedEndsAt.toDouble()
}
