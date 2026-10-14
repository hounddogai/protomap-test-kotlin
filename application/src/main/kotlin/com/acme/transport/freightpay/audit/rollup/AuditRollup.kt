package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit rollup records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit rollup is in its life cycle. */
enum class AuditRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One audit rollup, with what the planners need to act on it. */
data class AuditRollup(
    var code: String,
    var title: String,
    var summary: String,
    var margin: Double,
    var pallets: Int,
    var externalRef: String,
    var locale: String,
    var retries: Int,
    var batchSize: Int,
    var status: AuditRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditRollupStatus): Boolean {
        when (status) {
            AuditRollupStatus.PLANNED -> return next == AuditRollupStatus.COMPLETED
            AuditRollupStatus.SCHEDULED -> {
                when (next) {
                    AuditRollupStatus.IN_PROGRESS, AuditRollupStatus.CANCELLED -> return true
                }
                return false
            }
            AuditRollupStatus.IN_PROGRESS -> {
                when (next) {
                    AuditRollupStatus.COMPLETED -> return true
                }
                return false
            }
            AuditRollupStatus.COMPLETED -> {
                when (next) {
                    AuditRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditRollupStatus) {
        if (!canMoveTo(next)) {
            error("audit rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(margin * pallets.toDouble() * 100) / 100

    fun snapshot(): AuditRollup {
        return AuditRollup(
            code = code,
            title = title,
            summary = summary,
            margin = margin,
            pallets = pallets,
            externalRef = externalRef,
            locale = locale,
            retries = retries,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a audit rollup in status planned. */
fun newAuditRollup(code: String, title: String): AuditRollup {
    return AuditRollup(
        code = code,
        title = title,
        summary = "",
        margin = 0.0,
        pallets = 0,
        externalRef = "",
        locale = "",
        retries = 0,
        batchSize = 0,
        status = AuditRollupStatus.PLANNED,
    )
}
