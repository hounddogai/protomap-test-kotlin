package com.acme.transport.network.cutoff.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default cutoff audit records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a default cutoff audit is in its life cycle. */
enum class DefaultCutoffAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default cutoff audit. Its code is unique within a tenant. */
data class DefaultCutoffAudit(
    var code: String,
    var name: String,
    var lane: String,
    var currency: String,
    var margin: Double,
    var batchSize: Int,
    var status: DefaultCutoffAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffAuditStatus): Boolean {
        when (status) {
            DefaultCutoffAuditStatus.OPEN -> {
                when (next) {
                    DefaultCutoffAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultCutoffAuditStatus.IN_REVIEW -> {
                when (next) {
                    DefaultCutoffAuditStatus.RESOLVED, DefaultCutoffAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultCutoffAuditStatus.RESOLVED -> return next == DefaultCutoffAuditStatus.REOPENED || next == DefaultCutoffAuditStatus.ARCHIVED
            DefaultCutoffAuditStatus.REOPENED -> {
                when (next) {
                    DefaultCutoffAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCutoffAuditStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default cutoff audit"} $code"

    fun adjustedVolume(): Double = round(batchSize.toDouble() * margin * 100) / 100

    fun snapshot(): DefaultCutoffAudit {
        return DefaultCutoffAudit(
            code = code,
            name = name,
            lane = lane,
            currency = currency,
            margin = margin,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default cutoff audit in status open. */
fun newDefaultCutoffAudit(code: String, name: String): DefaultCutoffAudit {
    return DefaultCutoffAudit(
        code = code,
        name = name,
        lane = "",
        currency = "",
        margin = 0.0,
        batchSize = 0,
        status = DefaultCutoffAuditStatus.OPEN,
    )
}
