package com.acme.transport.crewing.roster.budget

/** How screens and exports show a batched roster template. */
data class BatchedRosterTemplateView(
    var code: String,
    var nameText: String,
    var version: String,
    var amountText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toBatchedRosterTemplateView(item: BatchedRosterTemplate): BatchedRosterTemplateView {
    return BatchedRosterTemplateView(
        code = item.code,
        nameText = item.name,
        version = item.version.toString(),
        amountText = "%.2f".format(item.amount),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterTemplateViews(items: MutableList<BatchedRosterTemplate>): MutableList<BatchedRosterTemplateView> {
    val views = mutableListOf<BatchedRosterTemplateView>()
    for (item in items) {
        views.add(toBatchedRosterTemplateView(item))
    }
    return views
}

fun sumBatchedRosterTemplateView(view: BatchedRosterTemplateView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedVersion.toDouble() + parsedAmount + parsedCreatedAt.toDouble()
}
