package com.acme.transport.linehaul.trailer.batch

/** How screens and exports show a trailer entry. */
data class TrailerEntryView(
    var code: String,
    var name: String,
    var channelText: String,
    var archived: String,
    var category: String,
    var score: String,
    var title: String,
    var ceiling: String,
    var amount: String,
    var statusLabel: String,
)

fun toTrailerEntryView(item: TrailerEntry): TrailerEntryView {
    return TrailerEntryView(
        code = item.code,
        name = item.name,
        channelText = item.channel,
        archived = "${item.archived}",
        category = item.category,
        score = "%.2f".format(item.score),
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerEntryViews(items: MutableList<TrailerEntry>): MutableList<TrailerEntryView> {
    val views = mutableListOf<TrailerEntryView>()
    for (item in items) {
        views.add(toTrailerEntryView(item))
    }
    return views
}

fun sumTrailerEntryView(view: TrailerEntryView): Double {
    val parsedScore = view.score.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedScore + parsedCeiling + parsedAmount
}
