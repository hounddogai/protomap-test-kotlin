package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict zone cycle is in its life cycle. */
enum class StrictZoneCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict zone cycle as the zone team keeps it. */
data class StrictZoneCycle(
    var code: String,
    var label: String,
    var phone: String,
    var retries: Int,
    var depot: String,
    var category: String,
    var ceiling: Double,
    var distanceKm: Double,
    var threshold: Double,
    var minimum: Double,
    var status: StrictZoneCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictZoneCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictZoneCycleStatus): Boolean {
        when (status) {
            StrictZoneCycleStatus.IDLE -> {
                when (next) {
                    StrictZoneCycleStatus.READY -> return true
                }
                return false
            }
            StrictZoneCycleStatus.WARMING -> {
                when (next) {
                    StrictZoneCycleStatus.READY -> return true
                }
                return false
            }
            StrictZoneCycleStatus.READY -> return next == StrictZoneCycleStatus.DRAINING
            StrictZoneCycleStatus.DRAINING -> {
                when (next) {
                    StrictZoneCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictZoneCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict zone cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(minimum * threshold * 100) / 100

    fun snapshot(): StrictZoneCycle {
        return StrictZoneCycle(
            code = code,
            label = label,
            phone = phone,
            retries = retries,
            depot = depot,
            category = category,
            ceiling = ceiling,
            distanceKm = distanceKm,
            threshold = threshold,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict zone cycle in status idle. */
fun newStrictZoneCycle(code: String, label: String): StrictZoneCycle {
    return StrictZoneCycle(
        code = code,
        label = label,
        phone = "",
        retries = 0,
        depot = "",
        category = "",
        ceiling = 0.0,
        distanceKm = 0.0,
        threshold = 0.0,
        minimum = 0.0,
        status = StrictZoneCycleStatus.IDLE,
    )
}
