package com.acme.transport.lastmile.parcel.segment

/** How screens and exports show a parcel digest. */
data class ParcelDigestView(
    var codeText: String,
    var title: String,
    var version: String,
    var visible: String,
    var countryText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toParcelDigestView(item: ParcelDigest): ParcelDigestView {
    return ParcelDigestView(
        codeText = item.code,
        title = item.title,
        version = item.version.toString(),
        visible = "${item.visible}",
        countryText = item.country,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelDigestViews(items: MutableList<ParcelDigest>): MutableList<ParcelDigestView> {
    val views = mutableListOf<ParcelDigestView>()
    for (item in items) {
        views.add(toParcelDigestView(item))
    }
    return views
}

fun sumParcelDigestView(view: ParcelDigestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedVersion.toDouble() + parsedStartsAt.toDouble()
}
