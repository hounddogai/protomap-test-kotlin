package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance policy is in its life cycle. */
enum class ClearancePolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a clearance policy and its current status. */
data class ClearancePolicy(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var rate: Double,
    var reviewer: String,
    var status: ClearancePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearancePolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearancePolicyStatus): Boolean {
        when (status) {
            ClearancePolicyStatus.IDLE -> {
                when (next) {
                    ClearancePolicyStatus.STOPPED -> return true
                }
                return false
            }
            ClearancePolicyStatus.WARMING -> {
                when (next) {
                    ClearancePolicyStatus.DRAINING -> return true
                }
                return false
            }
            ClearancePolicyStatus.READY -> return next == ClearancePolicyStatus.DRAINING || next == ClearancePolicyStatus.STOPPED
            ClearancePolicyStatus.DRAINING -> {
                when (next) {
                    ClearancePolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearancePolicyStatus) {
        if (!canMoveTo(next)) {
            error("clearance policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((rate + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ClearancePolicy {
        return ClearancePolicy(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            rate = rate,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance policy in status idle. */
fun newClearancePolicy(code: String, name: String): ClearancePolicy {
    return ClearancePolicy(
        code = code,
        name = name,
        durationMinutes = 0,
        rate = 0.0,
        reviewer = "",
        status = ClearancePolicyStatus.IDLE,
    )
}
