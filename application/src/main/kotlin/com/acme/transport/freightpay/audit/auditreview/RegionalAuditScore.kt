package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional audit score is in its life cycle. */
enum class RegionalAuditScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional audit score. Its code is unique within a tenant. */
data class RegionalAuditScore(
    var code: String,
    var label: String,
    var segment: String,
    var rate: Double,
    var depot: String,
    var attempts: Int,
    var quantity: Int,
    var status: RegionalAuditScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditScoreStatus): Boolean {
        when (status) {
            RegionalAuditScoreStatus.DRAFT -> {
                when (next) {
                    RegionalAuditScoreStatus.ACTIVE -> return true
                }
                return false
            }
            RegionalAuditScoreStatus.PENDING -> return next == RegionalAuditScoreStatus.ACTIVE || next == RegionalAuditScoreStatus.SUSPENDED
            RegionalAuditScoreStatus.ACTIVE -> return next == RegionalAuditScoreStatus.SUSPENDED
            RegionalAuditScoreStatus.SUSPENDED -> return next == RegionalAuditScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalAuditScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional audit score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional audit score"} $code"

    fun effectiveVolume(): Double = round((attempts.toDouble() + rate) * 100) / 100

    fun snapshot(): RegionalAuditScore {
        return RegionalAuditScore(
            code = code,
            label = label,
            segment = segment,
            rate = rate,
            depot = depot,
            attempts = attempts,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional audit score in status draft. */
fun newRegionalAuditScore(code: String, label: String): RegionalAuditScore {
    return RegionalAuditScore(
        code = code,
        label = label,
        segment = "",
        rate = 0.0,
        depot = "",
        attempts = 0,
        quantity = 0,
        status = RegionalAuditScoreStatus.DRAFT,
    )
}
