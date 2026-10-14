package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy clearance group is in its life cycle. */
enum class LegacyClearanceGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy clearance group and its current status. */
data class LegacyClearanceGroup(
    var code: String,
    var label: String,
    var approved: Boolean,
    var lane: String,
    var rate: Double,
    var ceiling: Double,
    var region: String,
    var status: LegacyClearanceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceGroupStatus): Boolean {
        when (status) {
            LegacyClearanceGroupStatus.NEW -> return next == LegacyClearanceGroupStatus.RUNNING || next == LegacyClearanceGroupStatus.DONE
            LegacyClearanceGroupStatus.QUEUED -> return next == LegacyClearanceGroupStatus.DONE
            LegacyClearanceGroupStatus.RUNNING -> return next == LegacyClearanceGroupStatus.DONE || next == LegacyClearanceGroupStatus.FAILED
            LegacyClearanceGroupStatus.DONE -> {
                when (next) {
                    LegacyClearanceGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyClearanceGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy clearance group"} $code"

    fun weightedLoad(): Double = round((rate - ceiling) * 100) / 100

    fun snapshot(): LegacyClearanceGroup {
        return LegacyClearanceGroup(
            code = code,
            label = label,
            approved = approved,
            lane = lane,
            rate = rate,
            ceiling = ceiling,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy clearance group in status new. */
fun newLegacyClearanceGroup(code: String, label: String): LegacyClearanceGroup {
    return LegacyClearanceGroup(
        code = code,
        label = label,
        approved = false,
        lane = "",
        rate = 0.0,
        ceiling = 0.0,
        region = "",
        status = LegacyClearanceGroupStatus.NEW,
    )
}
