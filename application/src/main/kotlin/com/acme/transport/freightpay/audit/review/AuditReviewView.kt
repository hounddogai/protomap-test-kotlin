package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit review. */
data class AuditReviewView(
    var code: String,
    var name: String,
    var region: String,
    var capacityText: String,
    var attempts: String,
    var externalRef: String,
    var limit: String,
    var archived: String,
    var statusLabel: String,
)

fun toAuditReviewView(item: AuditReview): AuditReviewView {
    return AuditReviewView(
        code = truncate(item.code, 80),
        name = item.name,
        region = truncate(item.region, 64),
        capacityText = item.capacity.toString(),
        attempts = item.attempts.toString(),
        externalRef = item.externalRef,
        limit = item.limit.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditReviewViews(items: MutableList<AuditReview>): MutableList<AuditReviewView> {
    val views = mutableListOf<AuditReviewView>()
    for (item in items) {
        views.add(toAuditReviewView(item))
    }
    return views
}

fun sumAuditReviewView(view: AuditReviewView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedCapacity.toDouble() + parsedAttempts.toDouble() + parsedLimit.toDouble()
}
