package com.acme.transport.linehaul.trailer.segment

/** How screens and exports show a capped trailer entry. */
data class CappedTrailerEntryView(
    var code: String,
    var name: String,
    var durationMinutes: String,
    var phone: String,
    var discount: String,
    var statusLabel: String,
)

fun toCappedTrailerEntryView(item: CappedTrailerEntry): CappedTrailerEntryView {
    return CappedTrailerEntryView(
        code = item.code,
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        phone = item.phone,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerEntryViews(items: MutableList<CappedTrailerEntry>): MutableList<CappedTrailerEntryView> {
    val views = mutableListOf<CappedTrailerEntryView>()
    for (item in items) {
        views.add(toCappedTrailerEntryView(item))
    }
    return views
}

fun sumCappedTrailerEntryView(view: CappedTrailerEntryView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedDurationMinutes.toDouble() + parsedDiscount
}
