package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive locker policy is in its life cycle. */
enum class AdaptiveLockerPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive locker policy, with what the planners need to act on it. */
data class AdaptiveLockerPolicy(
    var code: String,
    var name: String,
    var revision: Int,
    var priority: Int,
    var stopCount: Int,
    var vehicleId: String,
    var batchSize: Int,
    var driverId: String,
    var carrier: String,
    var summary: String,
    var minimum: Double,
    var status: AdaptiveLockerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerPolicyStatus): Boolean {
        when (status) {
            AdaptiveLockerPolicyStatus.PLANNED -> {
                when (next) {
                    AdaptiveLockerPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveLockerPolicyStatus.SCHEDULED -> return next == AdaptiveLockerPolicyStatus.COMPLETED || next == AdaptiveLockerPolicyStatus.CANCELLED
            AdaptiveLockerPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveLockerPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveLockerPolicyStatus.COMPLETED -> {
                when (next) {
                    AdaptiveLockerPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive locker policy"} $code"

    fun adjustedValue(): Double = round((batchSize.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveLockerPolicy {
        return AdaptiveLockerPolicy(
            code = code,
            name = name,
            revision = revision,
            priority = priority,
            stopCount = stopCount,
            vehicleId = vehicleId,
            batchSize = batchSize,
            driverId = driverId,
            carrier = carrier,
            summary = summary,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive locker policy in status planned. */
fun newAdaptiveLockerPolicy(code: String, name: String): AdaptiveLockerPolicy {
    return AdaptiveLockerPolicy(
        code = code,
        name = name,
        revision = 0,
        priority = 0,
        stopCount = 0,
        vehicleId = "",
        batchSize = 0,
        driverId = "",
        carrier = "",
        summary = "",
        minimum = 0.0,
        status = AdaptiveLockerPolicyStatus.PLANNED,
    )
}
