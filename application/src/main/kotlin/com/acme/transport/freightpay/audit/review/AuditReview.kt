package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit review records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit review is in its life cycle. */
enum class AuditReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a audit review and its current status. */
data class AuditReview(
    var code: String,
    var name: String,
    var region: String,
    var capacity: Int,
    var attempts: Int,
    var externalRef: String,
    var limit: Int,
    var tags: MutableList<String>,
    var archived: Boolean,
    var status: AuditReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditReviewStatus): Boolean {
        when (status) {
            AuditReviewStatus.NEW -> {
                when (next) {
                    AuditReviewStatus.DONE -> return true
                }
                return false
            }
            AuditReviewStatus.QUEUED -> return next == AuditReviewStatus.FAILED
            AuditReviewStatus.RUNNING -> return next == AuditReviewStatus.FAILED
            AuditReviewStatus.DONE -> return next == AuditReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AuditReviewStatus) {
        if (!canMoveTo(next)) {
            error("audit review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round(capacity.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): AuditReview {
        return AuditReview(
            code = code,
            name = name,
            region = region,
            capacity = capacity,
            attempts = attempts,
            externalRef = externalRef,
            limit = limit,
            tags = tags,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a audit review in status new. */
fun newAuditReview(code: String, name: String): AuditReview {
    return AuditReview(
        code = code,
        name = name,
        region = "",
        capacity = 0,
        attempts = 0,
        externalRef = "",
        limit = 0,
        tags = mutableListOf<String>(),
        archived = false,
        status = AuditReviewStatus.NEW,
    )
}
