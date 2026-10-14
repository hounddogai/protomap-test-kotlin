package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global ev charge plan is in its life cycle. */
enum class GlobalEvChargePlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global ev charge plan, with what the planners need to act on it. */
data class GlobalEvChargePlan(
    var code: String,
    var name: String,
    var carrier: String,
    var dock: String,
    var score: Double,
    var version: Int,
    var trailerId: String,
    var limit: Int,
    var discount: Double,
    var maximum: Double,
    var fuelLiters: Double,
    var status: GlobalEvChargePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEvChargePlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEvChargePlanStatus): Boolean {
        when (status) {
            GlobalEvChargePlanStatus.IDLE -> {
                when (next) {
                    GlobalEvChargePlanStatus.WARMING, GlobalEvChargePlanStatus.STOPPED -> return true
                }
                return false
            }
            GlobalEvChargePlanStatus.WARMING -> return next == GlobalEvChargePlanStatus.READY
            GlobalEvChargePlanStatus.READY -> return next == GlobalEvChargePlanStatus.STOPPED
            GlobalEvChargePlanStatus.DRAINING -> return next == GlobalEvChargePlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalEvChargePlanStatus) {
        if (!canMoveTo(next)) {
            error("global ev charge plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(fuelLiters * limit.toDouble() * 100) / 100

    fun snapshot(): GlobalEvChargePlan {
        return GlobalEvChargePlan(
            code = code,
            name = name,
            carrier = carrier,
            dock = dock,
            score = score,
            version = version,
            trailerId = trailerId,
            limit = limit,
            discount = discount,
            maximum = maximum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global ev charge plan in status idle. */
fun newGlobalEvChargePlan(code: String, name: String): GlobalEvChargePlan {
    return GlobalEvChargePlan(
        code = code,
        name = name,
        carrier = "",
        dock = "",
        score = 0.0,
        version = 0,
        trailerId = "",
        limit = 0,
        discount = 0.0,
        maximum = 0.0,
        fuelLiters = 0.0,
        status = GlobalEvChargePlanStatus.IDLE,
    )
}
