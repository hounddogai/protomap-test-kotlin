package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional broker audit is in its life cycle. */
enum class RegionalBrokerAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional broker audit, with what the planners need to act on it. */
data class RegionalBrokerAudit(
    var code: String,
    var title: String,
    var carrier: String,
    var updatedAt: Int,
    var batchSize: Int,
    var quantity: Int,
    var score: Double,
    var currency: String,
    var pinned: Boolean,
    var status: RegionalBrokerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerAuditStatus): Boolean {
        when (status) {
            RegionalBrokerAuditStatus.DRAFT -> return next == RegionalBrokerAuditStatus.PENDING
            RegionalBrokerAuditStatus.PENDING -> return next == RegionalBrokerAuditStatus.CLOSED
            RegionalBrokerAuditStatus.ACTIVE -> {
                when (next) {
                    RegionalBrokerAuditStatus.SUSPENDED, RegionalBrokerAuditStatus.CLOSED -> return true
                }
                return false
            }
            RegionalBrokerAuditStatus.SUSPENDED -> return next == RegionalBrokerAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalBrokerAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional broker audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional broker audit"} $code"

    fun weightedExposure(): Double = round((batchSize.toDouble() - score) * 100) / 100

    fun snapshot(): RegionalBrokerAudit {
        return RegionalBrokerAudit(
            code = code,
            title = title,
            carrier = carrier,
            updatedAt = updatedAt,
            batchSize = batchSize,
            quantity = quantity,
            score = score,
            currency = currency,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional broker audit in status draft. */
fun newRegionalBrokerAudit(code: String, title: String): RegionalBrokerAudit {
    return RegionalBrokerAudit(
        code = code,
        title = title,
        carrier = "",
        updatedAt = 0,
        batchSize = 0,
        quantity = 0,
        score = 0.0,
        currency = "",
        pinned = false,
        status = RegionalBrokerAuditStatus.DRAFT,
    )
}
