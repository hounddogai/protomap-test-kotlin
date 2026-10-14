package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback audit target is in its life cycle. */
enum class FallbackAuditTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback audit target as the audit team keeps it. */
data class FallbackAuditTarget(
    var code: String,
    var name: String,
    var region: String,
    var approved: Boolean,
    var ratio: Double,
    var graceDays: Int,
    var lane: String,
    var notes: String,
    var startsAt: Int,
    var shiftCode: String,
    var routeCode: String,
    var status: FallbackAuditTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAuditTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAuditTargetStatus): Boolean {
        when (status) {
            FallbackAuditTargetStatus.IDLE -> return next == FallbackAuditTargetStatus.READY
            FallbackAuditTargetStatus.WARMING -> return next == FallbackAuditTargetStatus.STOPPED
            FallbackAuditTargetStatus.READY -> {
                when (next) {
                    FallbackAuditTargetStatus.DRAINING -> return true
                }
                return false
            }
            FallbackAuditTargetStatus.DRAINING -> {
                when (next) {
                    FallbackAuditTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAuditTargetStatus) {
        if (!canMoveTo(next)) {
            error("fallback audit target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback audit target"} $code"

    fun effectiveValue(): Double = round((ratio - graceDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackAuditTarget {
        return FallbackAuditTarget(
            code = code,
            name = name,
            region = region,
            approved = approved,
            ratio = ratio,
            graceDays = graceDays,
            lane = lane,
            notes = notes,
            startsAt = startsAt,
            shiftCode = shiftCode,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback audit target in status idle. */
fun newFallbackAuditTarget(code: String, name: String): FallbackAuditTarget {
    return FallbackAuditTarget(
        code = code,
        name = name,
        region = "",
        approved = false,
        ratio = 0.0,
        graceDays = 0,
        lane = "",
        notes = "",
        startsAt = 0,
        shiftCode = "",
        routeCode = "",
        status = FallbackAuditTargetStatus.IDLE,
    )
}
