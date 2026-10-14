package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling broker adjustment records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a rolling broker adjustment is in its life cycle. */
enum class RollingBrokerAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling broker adjustment, with what the planners need to act on it. */
data class RollingBrokerAdjustment(
    var code: String,
    var name: String,
    var priority: Int,
    var maximum: Double,
    var contactEmail: String,
    var ceiling: Double,
    var etaMinutes: Int,
    var status: RollingBrokerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBrokerAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBrokerAdjustmentStatus): Boolean {
        when (status) {
            RollingBrokerAdjustmentStatus.NEW -> return next == RollingBrokerAdjustmentStatus.QUEUED || next == RollingBrokerAdjustmentStatus.RUNNING
            RollingBrokerAdjustmentStatus.QUEUED -> {
                when (next) {
                    RollingBrokerAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            RollingBrokerAdjustmentStatus.RUNNING -> {
                when (next) {
                    RollingBrokerAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            RollingBrokerAdjustmentStatus.DONE -> {
                when (next) {
                    RollingBrokerAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingBrokerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling broker adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling broker adjustment"} $code"

    fun projectedExposure(): Double = round((priority.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingBrokerAdjustment {
        return RollingBrokerAdjustment(
            code = code,
            name = name,
            priority = priority,
            maximum = maximum,
            contactEmail = contactEmail,
            ceiling = ceiling,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling broker adjustment in status new. */
fun newRollingBrokerAdjustment(code: String, name: String): RollingBrokerAdjustment {
    return RollingBrokerAdjustment(
        code = code,
        name = name,
        priority = 0,
        maximum = 0.0,
        contactEmail = "",
        ceiling = 0.0,
        etaMinutes = 0,
        status = RollingBrokerAdjustmentStatus.NEW,
    )
}
