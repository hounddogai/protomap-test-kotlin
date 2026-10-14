package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service area audit is in its life cycle. */
enum class FallbackServiceAreaAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback service area audit as the service area team keeps it. */
data class FallbackServiceAreaAudit(
    var code: String,
    var title: String,
    var score: Double,
    var currency: String,
    var batchSize: Int,
    var ownerId: String,
    var version: Int,
    var graceDays: Int,
    var notes: String,
    var name: String,
    var status: FallbackServiceAreaAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServiceAreaAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServiceAreaAuditStatus): Boolean {
        when (status) {
            FallbackServiceAreaAuditStatus.OPEN -> return next == FallbackServiceAreaAuditStatus.RESOLVED || next == FallbackServiceAreaAuditStatus.ARCHIVED
            FallbackServiceAreaAuditStatus.IN_REVIEW -> {
                when (next) {
                    FallbackServiceAreaAuditStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackServiceAreaAuditStatus.RESOLVED -> {
                when (next) {
                    FallbackServiceAreaAuditStatus.REOPENED, FallbackServiceAreaAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackServiceAreaAuditStatus.REOPENED -> return next == FallbackServiceAreaAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackServiceAreaAuditStatus) {
        if (!canMoveTo(next)) {
            error("fallback service area audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((batchSize.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackServiceAreaAudit {
        return FallbackServiceAreaAudit(
            code = code,
            title = title,
            score = score,
            currency = currency,
            batchSize = batchSize,
            ownerId = ownerId,
            version = version,
            graceDays = graceDays,
            notes = notes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback service area audit in status open. */
fun newFallbackServiceAreaAudit(code: String, title: String): FallbackServiceAreaAudit {
    return FallbackServiceAreaAudit(
        code = code,
        title = title,
        score = 0.0,
        currency = "",
        batchSize = 0,
        ownerId = "",
        version = 0,
        graceDays = 0,
        notes = "",
        name = "",
        status = FallbackServiceAreaAuditStatus.OPEN,
    )
}
