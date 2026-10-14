package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tire plan is in its life cycle. */
enum class CappedTirePlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped tire plan. Its code is unique within a tenant. */
data class CappedTirePlan(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var endsAt: Int,
    var windowDays: Int,
    var status: CappedTirePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTirePlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTirePlanStatus): Boolean {
        when (status) {
            CappedTirePlanStatus.IDLE -> return next == CappedTirePlanStatus.WARMING
            CappedTirePlanStatus.WARMING -> return next == CappedTirePlanStatus.READY || next == CappedTirePlanStatus.DRAINING
            CappedTirePlanStatus.READY -> {
                when (next) {
                    CappedTirePlanStatus.DRAINING -> return true
                }
                return false
            }
            CappedTirePlanStatus.DRAINING -> return next == CappedTirePlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedTirePlanStatus) {
        if (!canMoveTo(next)) {
            error("capped tire plan $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((endsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CappedTirePlan {
        return CappedTirePlan(
            code = code,
            title = title,
            expiresAt = expiresAt,
            endsAt = endsAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped tire plan in status idle. */
fun newCappedTirePlan(code: String, title: String): CappedTirePlan {
    return CappedTirePlan(
        code = code,
        title = title,
        expiresAt = 0,
        endsAt = 0,
        windowDays = 0,
        status = CappedTirePlanStatus.IDLE,
    )
}
