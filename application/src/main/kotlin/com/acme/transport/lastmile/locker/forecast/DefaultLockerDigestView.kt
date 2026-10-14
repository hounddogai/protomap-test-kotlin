package com.acme.transport.lastmile.locker.forecast

/** How screens and exports show a default locker digest. */
data class DefaultLockerDigestView(
    var code: String,
    var nameText: String,
    var label: String,
    var endsAtText: String,
    var ceilingText: String,
    var locale: String,
    var pinned: String,
    var etaMinutes: String,
    var minimum: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toDefaultLockerDigestView(item: DefaultLockerDigest): DefaultLockerDigestView {
    return DefaultLockerDigestView(
        code = item.code,
        nameText = item.name,
        label = item.label,
        endsAtText = item.endsAt.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        locale = item.locale,
        pinned = "${item.pinned}",
        etaMinutes = item.etaMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultLockerDigestViews(items: MutableList<DefaultLockerDigest>): MutableList<DefaultLockerDigestView> {
    val views = mutableListOf<DefaultLockerDigestView>()
    for (item in items) {
        views.add(toDefaultLockerDigestView(item))
    }
    return views
}

fun sumDefaultLockerDigestView(view: DefaultLockerDigestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedCeiling + parsedEtaMinutes.toDouble()
}
