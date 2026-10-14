package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation claim is in its life cycle. */
enum class ViolationClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a violation claim and its current status. */
data class ViolationClaim(
    var code: String,
    var title: String,
    var description: String,
    var startsAt: Int,
    var visible: Boolean,
    var unit: String,
    var maximum: Double,
    var odometer: Int,
    var batchSize: Int,
    var budget: Double,
    var status: ViolationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationClaimStatus): Boolean {
        when (status) {
            ViolationClaimStatus.IDLE -> return next == ViolationClaimStatus.DRAINING || next == ViolationClaimStatus.STOPPED
            ViolationClaimStatus.WARMING -> return next == ViolationClaimStatus.READY
            ViolationClaimStatus.READY -> {
                when (next) {
                    ViolationClaimStatus.STOPPED -> return true
                }
                return false
            }
            ViolationClaimStatus.DRAINING -> {
                when (next) {
                    ViolationClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationClaimStatus) {
        if (!canMoveTo(next)) {
            error("violation claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((startsAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ViolationClaim {
        return ViolationClaim(
            code = code,
            title = title,
            description = description,
            startsAt = startsAt,
            visible = visible,
            unit = unit,
            maximum = maximum,
            odometer = odometer,
            batchSize = batchSize,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a violation claim in status idle. */
fun newViolationClaim(code: String, title: String): ViolationClaim {
    return ViolationClaim(
        code = code,
        title = title,
        description = "",
        startsAt = 0,
        visible = false,
        unit = "",
        maximum = 0.0,
        odometer = 0,
        batchSize = 0,
        budget = 0.0,
        status = ViolationClaimStatus.IDLE,
    )
}
