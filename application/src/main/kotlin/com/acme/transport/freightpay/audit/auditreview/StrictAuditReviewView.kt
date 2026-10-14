package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict audit review. */
data class StrictAuditReviewView(
    var code: String,
    var label: String,
    var routeCode: String,
    var source: String,
    var locale: String,
    var windowDays: String,
    var contactEmailText: String,
    var depot: String,
    var statusLabel: String,
)

fun toStrictAuditReviewView(item: StrictAuditReview): StrictAuditReviewView {
    return StrictAuditReviewView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 64),
        routeCode = item.routeCode,
        source = item.source,
        locale = truncate(item.locale, 64),
        windowDays = item.windowDays.toString(),
        contactEmailText = item.contactEmail,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAuditReviewViews(items: MutableList<StrictAuditReview>): MutableList<StrictAuditReviewView> {
    val views = mutableListOf<StrictAuditReviewView>()
    for (item in items) {
        views.add(toStrictAuditReviewView(item))
    }
    return views
}

fun sumStrictAuditReviewView(view: StrictAuditReviewView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
