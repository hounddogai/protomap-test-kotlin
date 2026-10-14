package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped coaching digest. */
data class CappedCoachingDigestView(
    var code: String,
    var nameText: String,
    var phoneText: String,
    var archived: String,
    var summary: String,
    var amount: String,
    var statusLabel: String,
)

fun toCappedCoachingDigestView(item: CappedCoachingDigest): CappedCoachingDigestView {
    return CappedCoachingDigestView(
        code = truncate(item.code, 64),
        nameText = item.name,
        phoneText = item.phone,
        archived = "${item.archived}",
        summary = item.summary,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCoachingDigestViews(items: MutableList<CappedCoachingDigest>): MutableList<CappedCoachingDigestView> {
    val views = mutableListOf<CappedCoachingDigestView>()
    for (item in items) {
        views.add(toCappedCoachingDigestView(item))
    }
    return views
}

fun sumCappedCoachingDigestView(view: CappedCoachingDigestView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
