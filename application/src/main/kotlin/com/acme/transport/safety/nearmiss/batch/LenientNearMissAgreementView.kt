package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient near miss agreement. */
data class LenientNearMissAgreementView(
    var code: String,
    var name: String,
    var unit: String,
    var updatedAt: String,
    var minimum: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toLenientNearMissAgreementView(item: LenientNearMissAgreement): LenientNearMissAgreementView {
    return LenientNearMissAgreementView(
        code = item.code,
        name = item.name,
        unit = truncate(item.unit, 64),
        updatedAt = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNearMissAgreementViews(items: MutableList<LenientNearMissAgreement>): MutableList<LenientNearMissAgreementView> {
    val views = mutableListOf<LenientNearMissAgreementView>()
    for (item in items) {
        views.add(toLenientNearMissAgreementView(item))
    }
    return views
}

fun sumLenientNearMissAgreementView(view: LenientNearMissAgreementView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedUpdatedAt.toDouble() + parsedMinimum
}
