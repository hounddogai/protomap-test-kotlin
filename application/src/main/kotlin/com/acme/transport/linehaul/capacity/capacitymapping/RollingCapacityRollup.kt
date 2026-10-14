package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling capacity rollup is in its life cycle. */
enum class RollingCapacityRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling capacity rollup and its current status. */
data class RollingCapacityRollup(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var externalRef: String,
    var approved: Boolean,
    var label: String,
    var phone: String,
    var status: RollingCapacityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCapacityRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCapacityRollupStatus): Boolean {
        when (status) {
            RollingCapacityRollupStatus.PROPOSED -> return next == RollingCapacityRollupStatus.REJECTED || next == RollingCapacityRollupStatus.WITHDRAWN
            RollingCapacityRollupStatus.APPROVED -> {
                when (next) {
                    RollingCapacityRollupStatus.REJECTED -> return true
                }
                return false
            }
            RollingCapacityRollupStatus.REJECTED -> {
                when (next) {
                    RollingCapacityRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCapacityRollupStatus) {
        if (!canMoveTo(next)) {
            error("rolling capacity rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun effectiveVolume(): Double = round(windowDays.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): RollingCapacityRollup {
        return RollingCapacityRollup(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            externalRef = externalRef,
            approved = approved,
            label = label,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling capacity rollup in status proposed. */
fun newRollingCapacityRollup(code: String, title: String): RollingCapacityRollup {
    return RollingCapacityRollup(
        code = code,
        title = title,
        etaMinutes = 0,
        windowDays = 0,
        externalRef = "",
        approved = false,
        label = "",
        phone = "",
        status = RollingCapacityRollupStatus.PROPOSED,
    )
}
