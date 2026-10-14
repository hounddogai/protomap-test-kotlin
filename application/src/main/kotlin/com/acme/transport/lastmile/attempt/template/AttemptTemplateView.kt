package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt template. */
data class AttemptTemplateView(
    var code: String,
    var titleText: String,
    var rate: String,
    var maximum: String,
    var durationMinutes: String,
    var fuelLiters: String,
    var visible: String,
    var updatedAt: String,
    var score: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAttemptTemplateView(item: AttemptTemplate): AttemptTemplateView {
    return AttemptTemplateView(
        code = item.code,
        titleText = truncate(item.title, 80),
        rate = "%.2f".format(item.rate),
        maximum = "%.2f".format(item.maximum),
        durationMinutes = item.durationMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        visible = "${item.visible}",
        updatedAt = item.updatedAt.toString(),
        score = "%.2f".format(item.score),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptTemplateViews(items: MutableList<AttemptTemplate>): MutableList<AttemptTemplateView> {
    val views = mutableListOf<AttemptTemplateView>()
    for (item in items) {
        views.add(toAttemptTemplateView(item))
    }
    return views
}

fun sumAttemptTemplateView(view: AttemptTemplateView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedRate + parsedMaximum + parsedDurationMinutes.toDouble()
}
