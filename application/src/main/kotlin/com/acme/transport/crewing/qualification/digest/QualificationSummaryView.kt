package com.acme.transport.crewing.qualification.digest

/** How screens and exports show a qualification summary. */
data class QualificationSummaryView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var budgetText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toQualificationSummaryView(item: QualificationSummary): QualificationSummaryView {
    return QualificationSummaryView(
        code = item.code,
        name = item.name,
        endsAtText = item.endsAt.toString(),
        budgetText = "%.2f".format(item.budget),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationSummaryViews(items: MutableList<QualificationSummary>): MutableList<QualificationSummaryView> {
    val views = mutableListOf<QualificationSummaryView>()
    for (item in items) {
        views.add(toQualificationSummaryView(item))
    }
    return views
}

fun sumQualificationSummaryView(view: QualificationSummaryView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedEndsAt.toDouble() + parsedBudget
}
