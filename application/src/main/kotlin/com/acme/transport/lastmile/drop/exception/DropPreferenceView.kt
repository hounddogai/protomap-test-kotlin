package com.acme.transport.lastmile.drop.exception

/** How screens and exports show a drop preference. */
data class DropPreferenceView(
    var code: String,
    var label: String,
    var expiresAtText: String,
    var amount: String,
    var durationMinutes: String,
    var description: String,
    var phone: String,
    var dock: String,
    var capacity: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDropPreferenceView(item: DropPreference): DropPreferenceView {
    return DropPreferenceView(
        code = item.code,
        label = item.label,
        expiresAtText = item.expiresAt.toString(),
        amount = "%.2f".format(item.amount),
        durationMinutes = item.durationMinutes.toString(),
        description = item.description,
        phone = item.phone,
        dock = item.dock,
        capacity = item.capacity.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropPreferenceViews(items: MutableList<DropPreference>): MutableList<DropPreferenceView> {
    val views = mutableListOf<DropPreferenceView>()
    for (item in items) {
        views.add(toDropPreferenceView(item))
    }
    return views
}

fun sumDropPreferenceView(view: DropPreferenceView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedAmount + parsedDurationMinutes.toDouble()
}
