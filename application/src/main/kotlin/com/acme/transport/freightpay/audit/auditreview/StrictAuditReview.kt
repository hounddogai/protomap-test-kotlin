package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize

// The strict audit review records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a strict audit review is in its life cycle. */
enum class StrictAuditReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict audit review as the audit team keeps it. */
data class StrictAuditReview(
    var code: String,
    var label: String,
    var routeCode: String,
    var source: String,
    var locale: String,
    var windowDays: Int,
    var contactEmail: String,
    var depot: String,
    var status: StrictAuditReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAuditReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAuditReviewStatus): Boolean {
        when (status) {
            StrictAuditReviewStatus.NEW -> {
                when (next) {
                    StrictAuditReviewStatus.DONE -> return true
                }
                return false
            }
            StrictAuditReviewStatus.QUEUED -> return next == StrictAuditReviewStatus.DONE || next == StrictAuditReviewStatus.FAILED
            StrictAuditReviewStatus.RUNNING -> {
                when (next) {
                    StrictAuditReviewStatus.DONE -> return true
                }
                return false
            }
            StrictAuditReviewStatus.DONE -> {
                when (next) {
                    StrictAuditReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAuditReviewStatus) {
        if (!canMoveTo(next)) {
            error("strict audit review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): StrictAuditReview {
        return StrictAuditReview(
            code = code,
            label = label,
            routeCode = routeCode,
            source = source,
            locale = locale,
            windowDays = windowDays,
            contactEmail = contactEmail,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict audit review in status new. */
fun newStrictAuditReview(code: String, label: String): StrictAuditReview {
    return StrictAuditReview(
        code = code,
        label = label,
        routeCode = "",
        source = "",
        locale = "",
        windowDays = 0,
        contactEmail = "",
        depot = "",
        status = StrictAuditReviewStatus.NEW,
    )
}
