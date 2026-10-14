package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy qualification review. */
data class LegacyQualificationReviewView(
    var codeText: String,
    var title: String,
    var updatedAt: String,
    var channelText: String,
    var phone: String,
    var checksum: String,
    var currencyText: String,
    var active: String,
    var fuelLitersText: String,
    var segment: String,
    var statusLabel: String,
)

fun toLegacyQualificationReviewView(item: LegacyQualificationReview): LegacyQualificationReviewView {
    return LegacyQualificationReviewView(
        codeText = truncate(item.code, 120),
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        channelText = item.channel,
        phone = item.phone,
        checksum = item.checksum,
        currencyText = truncate(item.currency, 80),
        active = "${item.active}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        segment = truncate(item.segment, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyQualificationReviewViews(items: MutableList<LegacyQualificationReview>): MutableList<LegacyQualificationReviewView> {
    val views = mutableListOf<LegacyQualificationReviewView>()
    for (item in items) {
        views.add(toLegacyQualificationReviewView(item))
    }
    return views
}

fun sumLegacyQualificationReviewView(view: LegacyQualificationReviewView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedFuelLiters
}
