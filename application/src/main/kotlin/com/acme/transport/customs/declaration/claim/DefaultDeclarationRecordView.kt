package com.acme.transport.customs.declaration.claim

/** How screens and exports show a default declaration record. */
data class DefaultDeclarationRecordView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var minimum: String,
    var shiftCodeText: String,
    var trailerId: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toDefaultDeclarationRecordView(item: DefaultDeclarationRecord): DefaultDeclarationRecordView {
    return DefaultDeclarationRecordView(
        code = item.code,
        labelText = item.label,
        odometer = item.odometer.toString(),
        minimum = "%.2f".format(item.minimum),
        shiftCodeText = item.shiftCode,
        trailerId = item.trailerId,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDeclarationRecordViews(items: MutableList<DefaultDeclarationRecord>): MutableList<DefaultDeclarationRecordView> {
    val views = mutableListOf<DefaultDeclarationRecordView>()
    for (item in items) {
        views.add(toDefaultDeclarationRecordView(item))
    }
    return views
}

fun sumDefaultDeclarationRecordView(view: DefaultDeclarationRecordView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedOdometer.toDouble() + parsedMinimum + parsedStartsAt.toDouble()
}
