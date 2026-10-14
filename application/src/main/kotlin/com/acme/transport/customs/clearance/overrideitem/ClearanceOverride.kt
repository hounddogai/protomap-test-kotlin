package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance override records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance override is in its life cycle. */
enum class ClearanceOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One clearance override, with what the planners need to act on it. */
data class ClearanceOverride(
    var code: String,
    var name: String,
    var ratio: Double,
    var ceiling: Double,
    var ownerId: String,
    var revision: Int,
    var createdAt: Int,
    var capacity: Int,
    var status: ClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceOverrideStatus): Boolean {
        when (status) {
            ClearanceOverrideStatus.REQUESTED -> {
                when (next) {
                    ClearanceOverrideStatus.GRANTED, ClearanceOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceOverrideStatus.GRANTED -> {
                when (next) {
                    ClearanceOverrideStatus.REVOKED, ClearanceOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceOverrideStatus.REVOKED -> return next == ClearanceOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("clearance override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round((ratio + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ClearanceOverride {
        return ClearanceOverride(
            code = code,
            name = name,
            ratio = ratio,
            ceiling = ceiling,
            ownerId = ownerId,
            revision = revision,
            createdAt = createdAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance override in status requested. */
fun newClearanceOverride(code: String, name: String): ClearanceOverride {
    return ClearanceOverride(
        code = code,
        name = name,
        ratio = 0.0,
        ceiling = 0.0,
        ownerId = "",
        revision = 0,
        createdAt = 0,
        capacity = 0,
        status = ClearanceOverrideStatus.REQUESTED,
    )
}
