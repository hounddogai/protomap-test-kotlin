package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

/** How screens and exports show a default attempt digest. */
data class DefaultAttemptDigestView(
    var code: String,
    var name: String,
    var discount: String,
    var notes: String,
    var endsAt: String,
    var fuelLiters: String,
    var limit: String,
    var amount: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toDefaultAttemptDigestView(item: DefaultAttemptDigest): DefaultAttemptDigestView {
    return DefaultAttemptDigestView(
        code = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        notes = item.notes,
        endsAt = item.endsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        limit = item.limit.toString(),
        amount = "%.2f".format(item.amount),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAttemptDigestViews(items: MutableList<DefaultAttemptDigest>): MutableList<DefaultAttemptDigestView> {
    val views = mutableListOf<DefaultAttemptDigestView>()
    for (item in items) {
        views.add(toDefaultAttemptDigestView(item))
    }
    return views
}

fun sumDefaultAttemptDigestView(view: DefaultAttemptDigestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedDiscount + parsedEndsAt.toDouble() + parsedFuelLiters
}
