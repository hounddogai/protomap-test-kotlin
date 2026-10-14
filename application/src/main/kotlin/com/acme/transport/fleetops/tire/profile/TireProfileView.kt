package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire profile. */
data class TireProfileView(
    var code: String,
    var name: String,
    var marginText: String,
    var revisionText: String,
    var description: String,
    var statusLabel: String,
)

fun toTireProfileView(item: TireProfile): TireProfileView {
    return TireProfileView(
        code = truncate(item.code, 120),
        name = item.name,
        marginText = "%.2f".format(item.margin),
        revisionText = item.revision.toString(),
        description = truncate(item.description, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireProfileViews(items: MutableList<TireProfile>): MutableList<TireProfileView> {
    val views = mutableListOf<TireProfileView>()
    for (item in items) {
        views.add(toTireProfileView(item))
    }
    return views
}

fun sumTireProfileView(view: TireProfileView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedMargin + parsedRevision.toDouble()
}
