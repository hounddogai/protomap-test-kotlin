package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dwell plan is in its life cycle. */
enum class RollingDwellPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling dwell plan as the dwell team keeps it. */
data class RollingDwellPlan(
    var code: String,
    var name: String,
    var discount: Double,
    var source: String,
    var endsAt: Int,
    var margin: Double,
    var updatedAt: Int,
    var lane: String,
    var contactEmail: String,
    var checksum: String,
    var status: RollingDwellPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDwellPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDwellPlanStatus): Boolean {
        when (status) {
            RollingDwellPlanStatus.IDLE -> return next == RollingDwellPlanStatus.WARMING || next == RollingDwellPlanStatus.READY
            RollingDwellPlanStatus.WARMING -> return next == RollingDwellPlanStatus.READY
            RollingDwellPlanStatus.READY -> return next == RollingDwellPlanStatus.DRAINING || next == RollingDwellPlanStatus.STOPPED
            RollingDwellPlanStatus.DRAINING -> return next == RollingDwellPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingDwellPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling dwell plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(endsAt.toDouble() * discount * 100) / 100

    fun snapshot(): RollingDwellPlan {
        return RollingDwellPlan(
            code = code,
            name = name,
            discount = discount,
            source = source,
            endsAt = endsAt,
            margin = margin,
            updatedAt = updatedAt,
            lane = lane,
            contactEmail = contactEmail,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling dwell plan in status idle. */
fun newRollingDwellPlan(code: String, name: String): RollingDwellPlan {
    return RollingDwellPlan(
        code = code,
        name = name,
        discount = 0.0,
        source = "",
        endsAt = 0,
        margin = 0.0,
        updatedAt = 0,
        lane = "",
        contactEmail = "",
        checksum = "",
        status = RollingDwellPlanStatus.IDLE,
    )
}
