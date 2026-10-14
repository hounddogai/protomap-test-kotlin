package com.acme.transport.lastmile.drop.rollup

/** How screens and exports show a tiered drop notice. */
data class TieredDropNoticeView(
    var code: String,
    var title: String,
    var localeText: String,
    var externalRef: String,
    var fuelLiters: String,
    var score: String,
    var statusLabel: String,
)

fun toTieredDropNoticeView(item: TieredDropNotice): TieredDropNoticeView {
    return TieredDropNoticeView(
        code = item.code,
        title = item.title,
        localeText = item.locale,
        externalRef = item.externalRef,
        fuelLiters = "%.2f".format(item.fuelLiters),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDropNoticeViews(items: MutableList<TieredDropNotice>): MutableList<TieredDropNoticeView> {
    val views = mutableListOf<TieredDropNoticeView>()
    for (item in items) {
        views.add(toTieredDropNoticeView(item))
    }
    return views
}

fun sumTieredDropNoticeView(view: TieredDropNoticeView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedFuelLiters + parsedScore
}
