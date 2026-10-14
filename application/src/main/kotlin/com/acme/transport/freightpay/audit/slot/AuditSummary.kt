package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit summary is in its life cycle. */
enum class AuditSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit summary. Its code is unique within a tenant. */
data class AuditSummary(
    var code: String,
    var name: String,
    var archived: Boolean,
    var threshold: Double,
    var score: Double,
    var timezone: String,
    var revision: Int,
    var contactEmail: String,
    var status: AuditSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditSummaryStatus): Boolean {
        when (status) {
            AuditSummaryStatus.PROPOSED -> return next == AuditSummaryStatus.APPROVED || next == AuditSummaryStatus.WITHDRAWN
            AuditSummaryStatus.APPROVED -> return next == AuditSummaryStatus.REJECTED || next == AuditSummaryStatus.WITHDRAWN
            AuditSummaryStatus.REJECTED -> return next == AuditSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AuditSummaryStatus) {
        if (!canMoveTo(next)) {
            error("audit summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round(revision.toDouble() * threshold * 100) / 100

    fun snapshot(): AuditSummary {
        return AuditSummary(
            code = code,
            name = name,
            archived = archived,
            threshold = threshold,
            score = score,
            timezone = timezone,
            revision = revision,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a audit summary in status proposed. */
fun newAuditSummary(code: String, name: String): AuditSummary {
    return AuditSummary(
        code = code,
        name = name,
        archived = false,
        threshold = 0.0,
        score = 0.0,
        timezone = "",
        revision = 0,
        contactEmail = "",
        status = AuditSummaryStatus.PROPOSED,
    )
}
