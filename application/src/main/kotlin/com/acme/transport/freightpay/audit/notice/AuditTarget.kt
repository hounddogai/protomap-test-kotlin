package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit target is in its life cycle. */
enum class AuditTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A audit target. Its code is unique within a tenant. */
data class AuditTarget(
    var code: String,
    var name: String,
    var currency: String,
    var retries: Int,
    var depot: String,
    var source: String,
    var locale: String,
    var batchSize: Int,
    var shiftCode: String,
    var pinned: Boolean,
    var limit: Int,
    var status: AuditTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditTargetStatus): Boolean {
        when (status) {
            AuditTargetStatus.PLANNED -> {
                when (next) {
                    AuditTargetStatus.CANCELLED -> return true
                }
                return false
            }
            AuditTargetStatus.SCHEDULED -> return next == AuditTargetStatus.COMPLETED
            AuditTargetStatus.IN_PROGRESS -> {
                when (next) {
                    AuditTargetStatus.COMPLETED, AuditTargetStatus.CANCELLED -> return true
                }
                return false
            }
            AuditTargetStatus.COMPLETED -> {
                when (next) {
                    AuditTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditTargetStatus) {
        if (!canMoveTo(next)) {
            error("audit target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round(retries.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): AuditTarget {
        return AuditTarget(
            code = code,
            name = name,
            currency = currency,
            retries = retries,
            depot = depot,
            source = source,
            locale = locale,
            batchSize = batchSize,
            shiftCode = shiftCode,
            pinned = pinned,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a audit target in status planned. */
fun newAuditTarget(code: String, name: String): AuditTarget {
    return AuditTarget(
        code = code,
        name = name,
        currency = "",
        retries = 0,
        depot = "",
        source = "",
        locale = "",
        batchSize = 0,
        shiftCode = "",
        pinned = false,
        limit = 0,
        status = AuditTargetStatus.PLANNED,
    )
}
