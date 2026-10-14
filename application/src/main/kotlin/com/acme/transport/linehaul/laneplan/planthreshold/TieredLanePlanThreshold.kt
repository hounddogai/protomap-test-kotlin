package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered lane plan threshold records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a tiered lane plan threshold is in its life cycle. */
enum class TieredLanePlanThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered lane plan threshold, with what the planners need to act on it. */
data class TieredLanePlanThreshold(
    var code: String,
    var name: String,
    var ownerId: String,
    var phone: String,
    var vehicleId: String,
    var retries: Int,
    var priority: Int,
    var batchSize: Int,
    var status: TieredLanePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLanePlanThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLanePlanThresholdStatus): Boolean {
        when (status) {
            TieredLanePlanThresholdStatus.IDLE -> {
                when (next) {
                    TieredLanePlanThresholdStatus.DRAINING -> return true
                }
                return false
            }
            TieredLanePlanThresholdStatus.WARMING -> return next == TieredLanePlanThresholdStatus.READY
            TieredLanePlanThresholdStatus.READY -> {
                when (next) {
                    TieredLanePlanThresholdStatus.STOPPED -> return true
                }
                return false
            }
            TieredLanePlanThresholdStatus.DRAINING -> return next == TieredLanePlanThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredLanePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tiered lane plan threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((retries.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TieredLanePlanThreshold {
        return TieredLanePlanThreshold(
            code = code,
            name = name,
            ownerId = ownerId,
            phone = phone,
            vehicleId = vehicleId,
            retries = retries,
            priority = priority,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered lane plan threshold in status idle. */
fun newTieredLanePlanThreshold(code: String, name: String): TieredLanePlanThreshold {
    return TieredLanePlanThreshold(
        code = code,
        name = name,
        ownerId = "",
        phone = "",
        vehicleId = "",
        retries = 0,
        priority = 0,
        batchSize = 0,
        status = TieredLanePlanThresholdStatus.IDLE,
    )
}
