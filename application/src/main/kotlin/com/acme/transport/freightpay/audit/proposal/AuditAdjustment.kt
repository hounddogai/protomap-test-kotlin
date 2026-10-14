package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit adjustment is in its life cycle. */
enum class AuditAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A audit adjustment as the audit team keeps it. */
data class AuditAdjustment(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var approved: Boolean,
    var country: String,
    var maximum: Double,
    var priority: Int,
    var graceDays: Int,
    var retries: Int,
    var status: AuditAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditAdjustmentStatus): Boolean {
        when (status) {
            AuditAdjustmentStatus.IDLE -> {
                when (next) {
                    AuditAdjustmentStatus.WARMING -> return true
                }
                return false
            }
            AuditAdjustmentStatus.WARMING -> return next == AuditAdjustmentStatus.DRAINING || next == AuditAdjustmentStatus.STOPPED
            AuditAdjustmentStatus.READY -> {
                when (next) {
                    AuditAdjustmentStatus.DRAINING, AuditAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            AuditAdjustmentStatus.DRAINING -> {
                when (next) {
                    AuditAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("audit adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"audit adjustment"} $code"

    fun adjustedTotal(): Double = round((graceDays.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): AuditAdjustment {
        return AuditAdjustment(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            approved = approved,
            country = country,
            maximum = maximum,
            priority = priority,
            graceDays = graceDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a audit adjustment in status idle. */
fun newAuditAdjustment(code: String, name: String): AuditAdjustment {
    return AuditAdjustment(
        code = code,
        name = name,
        durationMinutes = 0,
        approved = false,
        country = "",
        maximum = 0.0,
        priority = 0,
        graceDays = 0,
        retries = 0,
        status = AuditAdjustmentStatus.IDLE,
    )
}
