package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending clearance override is in its life cycle. */
enum class PendingClearanceOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending clearance override, with what the planners need to act on it. */
data class PendingClearanceOverride(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var lane: String,
    var active: Boolean,
    var discount: Double,
    var tags: MutableList<String>,
    var capacity: Int,
    var carrier: String,
    var country: String,
    var cost: Double,
    var status: PendingClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingClearanceOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingClearanceOverrideStatus): Boolean {
        when (status) {
            PendingClearanceOverrideStatus.NEW -> return next == PendingClearanceOverrideStatus.RUNNING || next == PendingClearanceOverrideStatus.FAILED
            PendingClearanceOverrideStatus.QUEUED -> {
                when (next) {
                    PendingClearanceOverrideStatus.RUNNING -> return true
                }
                return false
            }
            PendingClearanceOverrideStatus.RUNNING -> {
                when (next) {
                    PendingClearanceOverrideStatus.FAILED -> return true
                }
                return false
            }
            PendingClearanceOverrideStatus.DONE -> return next == PendingClearanceOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending clearance override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((etaMinutes.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingClearanceOverride {
        return PendingClearanceOverride(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            lane = lane,
            active = active,
            discount = discount,
            tags = tags,
            capacity = capacity,
            carrier = carrier,
            country = country,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending clearance override in status new. */
fun newPendingClearanceOverride(code: String, label: String): PendingClearanceOverride {
    return PendingClearanceOverride(
        code = code,
        label = label,
        etaMinutes = 0,
        lane = "",
        active = false,
        discount = 0.0,
        tags = mutableListOf<String>(),
        capacity = 0,
        carrier = "",
        country = "",
        cost = 0.0,
        status = PendingClearanceOverrideStatus.NEW,
    )
}
