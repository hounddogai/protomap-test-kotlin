package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit threshold is in its life cycle. */
enum class AuditThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A audit threshold. Its code is unique within a tenant. */
data class AuditThreshold(
    var code: String,
    var name: String,
    var score: Double,
    var limit: Int,
    var carrier: String,
    var vehicleId: String,
    var active: Boolean,
    var rate: Double,
    var status: AuditThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditThresholdStatus): Boolean {
        when (status) {
            AuditThresholdStatus.IDLE -> return next == AuditThresholdStatus.WARMING
            AuditThresholdStatus.WARMING -> {
                when (next) {
                    AuditThresholdStatus.DRAINING -> return true
                }
                return false
            }
            AuditThresholdStatus.READY -> return next == AuditThresholdStatus.DRAINING || next == AuditThresholdStatus.STOPPED
            AuditThresholdStatus.DRAINING -> {
                when (next) {
                    AuditThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditThresholdStatus) {
        if (!canMoveTo(next)) {
            error("audit threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"audit threshold"} $code"

    fun weightedExposure(): Double = round((limit.toDouble() + score) * 100) / 100

    fun snapshot(): AuditThreshold {
        return AuditThreshold(
            code = code,
            name = name,
            score = score,
            limit = limit,
            carrier = carrier,
            vehicleId = vehicleId,
            active = active,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a audit threshold in status idle. */
fun newAuditThreshold(code: String, name: String): AuditThreshold {
    return AuditThreshold(
        code = code,
        name = name,
        score = 0.0,
        limit = 0,
        carrier = "",
        vehicleId = "",
        active = false,
        rate = 0.0,
        status = AuditThresholdStatus.IDLE,
    )
}
