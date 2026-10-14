package com.acme.transport.emissions.idling.window

/** How screens and exports show a idling window. */
data class IdlingWindowView(
    var code: String,
    var title: String,
    var revisionText: String,
    var countryText: String,
    var carrier: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toIdlingWindowView(item: IdlingWindow): IdlingWindowView {
    return IdlingWindowView(
        code = item.code,
        title = item.title,
        revisionText = item.revision.toString(),
        countryText = item.country,
        carrier = item.carrier,
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingWindowViews(items: MutableList<IdlingWindow>): MutableList<IdlingWindowView> {
    val views = mutableListOf<IdlingWindowView>()
    for (item in items) {
        views.add(toIdlingWindowView(item))
    }
    return views
}

fun sumIdlingWindowView(view: IdlingWindowView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}
