package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy backhaul allocation records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a legacy backhaul allocation is in its life cycle. */
enum class LegacyBackhaulAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy backhaul allocation. Its code is unique within a tenant. */
data class LegacyBackhaulAllocation(
    var code: String,
    var name: String,
    var score: Double,
    var loadKg: Double,
    var pinned: Boolean,
    var status: LegacyBackhaulAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulAllocationStatus): Boolean {
        when (status) {
            LegacyBackhaulAllocationStatus.NEW -> return next == LegacyBackhaulAllocationStatus.RUNNING || next == LegacyBackhaulAllocationStatus.DONE
            LegacyBackhaulAllocationStatus.QUEUED -> return next == LegacyBackhaulAllocationStatus.DONE || next == LegacyBackhaulAllocationStatus.FAILED
            LegacyBackhaulAllocationStatus.RUNNING -> {
                when (next) {
                    LegacyBackhaulAllocationStatus.DONE -> return true
                }
                return false
            }
            LegacyBackhaulAllocationStatus.DONE -> {
                when (next) {
                    LegacyBackhaulAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulAllocationStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy backhaul allocation"} $code"

    fun adjustedExposure(): Double = round((loadKg + score) * 100) / 100

    fun snapshot(): LegacyBackhaulAllocation {
        return LegacyBackhaulAllocation(
            code = code,
            name = name,
            score = score,
            loadKg = loadKg,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy backhaul allocation in status new. */
fun newLegacyBackhaulAllocation(code: String, name: String): LegacyBackhaulAllocation {
    return LegacyBackhaulAllocation(
        code = code,
        name = name,
        score = 0.0,
        loadKg = 0.0,
        pinned = false,
        status = LegacyBackhaulAllocationStatus.NEW,
    )
}
