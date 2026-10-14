package com.acme.transport.lastmile.parcel.parcelslot

/** How screens and exports show a scheduled parcel digest. */
data class ScheduledParcelDigestView(
    var codeText: String,
    var label: String,
    var updatedAt: String,
    var externalRefText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toScheduledParcelDigestView(item: ScheduledParcelDigest): ScheduledParcelDigestView {
    return ScheduledParcelDigestView(
        codeText = item.code,
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        externalRefText = item.externalRef,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledParcelDigestViews(items: MutableList<ScheduledParcelDigest>): MutableList<ScheduledParcelDigestView> {
    val views = mutableListOf<ScheduledParcelDigestView>()
    for (item in items) {
        views.add(toScheduledParcelDigestView(item))
    }
    return views
}

fun sumScheduledParcelDigestView(view: ScheduledParcelDigestView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedUpdatedAt.toDouble() + parsedEndsAt.toDouble()
}
