package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster review. */
data class RosterReviewView(
    var codeText: String,
    var nameText: String,
    var distanceKm: String,
    var budgetText: String,
    var country: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toRosterReviewView(item: RosterReview): RosterReviewView {
    return RosterReviewView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        budgetText = "%.2f".format(item.budget),
        country = item.country,
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterReviewViews(items: MutableList<RosterReview>): MutableList<RosterReviewView> {
    val views = mutableListOf<RosterReviewView>()
    for (item in items) {
        views.add(toRosterReviewView(item))
    }
    return views
}

fun sumRosterReviewView(view: RosterReviewView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedDistanceKm + parsedBudget
}
