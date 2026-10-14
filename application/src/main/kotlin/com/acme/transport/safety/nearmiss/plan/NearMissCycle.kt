package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss cycle is in its life cycle. */
enum class NearMissCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A near miss cycle as the near miss team keeps it. */
data class NearMissCycle(
    var code: String,
    var label: String,
    var limit: Int,
    var odometer: Int,
    var vehicleId: String,
    var startsAt: Int,
    var retries: Int,
    var graceDays: Int,
    var lane: String,
    var status: NearMissCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissCycleStatus): Boolean {
        when (status) {
            NearMissCycleStatus.CREATED -> {
                when (next) {
                    NearMissCycleStatus.VALIDATED, NearMissCycleStatus.RETIRED -> return true
                }
                return false
            }
            NearMissCycleStatus.VALIDATED -> return next == NearMissCycleStatus.PUBLISHED || next == NearMissCycleStatus.RETIRED
            NearMissCycleStatus.PUBLISHED -> return next == NearMissCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NearMissCycleStatus) {
        if (!canMoveTo(next)) {
            error("near miss cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round(retries.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): NearMissCycle {
        return NearMissCycle(
            code = code,
            label = label,
            limit = limit,
            odometer = odometer,
            vehicleId = vehicleId,
            startsAt = startsAt,
            retries = retries,
            graceDays = graceDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss cycle in status created. */
fun newNearMissCycle(code: String, label: String): NearMissCycle {
    return NearMissCycle(
        code = code,
        label = label,
        limit = 0,
        odometer = 0,
        vehicleId = "",
        startsAt = 0,
        retries = 0,
        graceDays = 0,
        lane = "",
        status = NearMissCycleStatus.CREATED,
    )
}
