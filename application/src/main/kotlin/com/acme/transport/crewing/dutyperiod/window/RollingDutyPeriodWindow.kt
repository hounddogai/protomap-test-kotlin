package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling duty period window records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a rolling duty period window is in its life cycle. */
enum class RollingDutyPeriodWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling duty period window. Its code is unique within a tenant. */
data class RollingDutyPeriodWindow(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var weight: Double,
    var ratio: Double,
    var threshold: Double,
    var channel: String,
    var checksum: String,
    var status: RollingDutyPeriodWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodWindowStatus): Boolean {
        when (status) {
            RollingDutyPeriodWindowStatus.DRAFT -> {
                when (next) {
                    RollingDutyPeriodWindowStatus.ACTIVE -> return true
                }
                return false
            }
            RollingDutyPeriodWindowStatus.PENDING -> {
                when (next) {
                    RollingDutyPeriodWindowStatus.SUSPENDED, RollingDutyPeriodWindowStatus.CLOSED -> return true
                }
                return false
            }
            RollingDutyPeriodWindowStatus.ACTIVE -> {
                when (next) {
                    RollingDutyPeriodWindowStatus.SUSPENDED, RollingDutyPeriodWindowStatus.CLOSED -> return true
                }
                return false
            }
            RollingDutyPeriodWindowStatus.SUSPENDED -> {
                when (next) {
                    RollingDutyPeriodWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): RollingDutyPeriodWindow {
        return RollingDutyPeriodWindow(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            weight = weight,
            ratio = ratio,
            threshold = threshold,
            channel = channel,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling duty period window in status draft. */
fun newRollingDutyPeriodWindow(code: String, name: String): RollingDutyPeriodWindow {
    return RollingDutyPeriodWindow(
        code = code,
        name = name,
        fuelLiters = 0.0,
        weight = 0.0,
        ratio = 0.0,
        threshold = 0.0,
        channel = "",
        checksum = "",
        status = RollingDutyPeriodWindowStatus.DRAFT,
    )
}
