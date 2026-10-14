package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration review. */
data class DeclarationReviewView(
    var code: String,
    var title: String,
    var locale: String,
    var segmentText: String,
    var approved: String,
    var maximumText: String,
    var label: String,
    var dockText: String,
    var durationMinutes: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toDeclarationReviewView(item: DeclarationReview): DeclarationReviewView {
    return DeclarationReviewView(
        code = truncate(item.code, 64),
        title = item.title,
        locale = item.locale,
        segmentText = item.segment,
        approved = "${item.approved}",
        maximumText = "%.2f".format(item.maximum),
        label = item.label,
        dockText = item.dock,
        durationMinutes = item.durationMinutes.toString(),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationReviewViews(items: MutableList<DeclarationReview>): MutableList<DeclarationReviewView> {
    val views = mutableListOf<DeclarationReviewView>()
    for (item in items) {
        views.add(toDeclarationReviewView(item))
    }
    return views
}

fun sumDeclarationReviewView(view: DeclarationReviewView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMaximum + parsedDurationMinutes.toDouble()
}
