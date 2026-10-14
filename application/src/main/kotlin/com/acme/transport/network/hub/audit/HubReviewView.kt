package com.acme.transport.network.hub.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub review. */
data class HubReviewView(
    var code: String,
    var titleText: String,
    var routeCode: String,
    var phone: String,
    var pallets: String,
    var rate: String,
    var dock: String,
    var limitText: String,
    var approved: String,
    var timezoneText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toHubReviewView(item: HubReview): HubReviewView {
    return HubReviewView(
        code = truncate(item.code, 64),
        titleText = item.title,
        routeCode = item.routeCode,
        phone = truncate(item.phone, 120),
        pallets = item.pallets.toString(),
        rate = "%.2f".format(item.rate),
        dock = item.dock,
        limitText = item.limit.toString(),
        approved = "${item.approved}",
        timezoneText = item.timezone,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubReviewViews(items: MutableList<HubReview>): MutableList<HubReviewView> {
    val views = mutableListOf<HubReviewView>()
    for (item in items) {
        views.add(toHubReviewView(item))
    }
    return views
}

fun sumHubReviewView(view: HubReviewView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedPallets.toDouble() + parsedRate + parsedLimit.toDouble()
}
