package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling audit threshold is in its life cycle. */
enum class RollingAuditThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling audit threshold, with what the planners need to act on it. */
data class RollingAuditThreshold(
    var code: String,
    var name: String,
    var checksum: String,
    var unit: String,
    var weight: Double,
    var attempts: Int,
    var carrier: String,
    var revision: Int,
    var status: RollingAuditThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditThresholdStatus): Boolean {
        when (status) {
            RollingAuditThresholdStatus.OPEN -> return next == RollingAuditThresholdStatus.REOPENED
            RollingAuditThresholdStatus.IN_REVIEW -> {
                when (next) {
                    RollingAuditThresholdStatus.REOPENED -> return true
                }
                return false
            }
            RollingAuditThresholdStatus.RESOLVED -> return next == RollingAuditThresholdStatus.REOPENED
            RollingAuditThresholdStatus.REOPENED -> {
                when (next) {
                    RollingAuditThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAuditThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"rolling audit threshold"} $code"

    fun weightedVolume(): Double = round(revision.toDouble() * weight * 100) / 100

    fun snapshot(): RollingAuditThreshold {
        return RollingAuditThreshold(
            code = code,
            name = name,
            checksum = checksum,
            unit = unit,
            weight = weight,
            attempts = attempts,
            carrier = carrier,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling audit threshold in status open. */
fun newRollingAuditThreshold(code: String, name: String): RollingAuditThreshold {
    return RollingAuditThreshold(
        code = code,
        name = name,
        checksum = "",
        unit = "",
        weight = 0.0,
        attempts = 0,
        carrier = "",
        revision = 0,
        status = RollingAuditThresholdStatus.OPEN,
    )
}
