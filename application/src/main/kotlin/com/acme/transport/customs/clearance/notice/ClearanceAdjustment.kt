package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance adjustment is in its life cycle. */
enum class ClearanceAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a clearance adjustment and its current status. */
data class ClearanceAdjustment(
    var code: String,
    var title: String,
    var score: Double,
    var lane: String,
    var floorValue: Double,
    var expiresAt: Int,
    var status: ClearanceAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceAdjustmentStatus): Boolean {
        when (status) {
            ClearanceAdjustmentStatus.REQUESTED -> {
                when (next) {
                    ClearanceAdjustmentStatus.GRANTED -> return true
                }
                return false
            }
            ClearanceAdjustmentStatus.GRANTED -> {
                when (next) {
                    ClearanceAdjustmentStatus.REVOKED, ClearanceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceAdjustmentStatus.REVOKED -> {
                when (next) {
                    ClearanceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("clearance adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round(score * floorValue * 100) / 100

    fun snapshot(): ClearanceAdjustment {
        return ClearanceAdjustment(
            code = code,
            title = title,
            score = score,
            lane = lane,
            floorValue = floorValue,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a clearance adjustment in status requested. */
fun newClearanceAdjustment(code: String, title: String): ClearanceAdjustment {
    return ClearanceAdjustment(
        code = code,
        title = title,
        score = 0.0,
        lane = "",
        floorValue = 0.0,
        expiresAt = 0,
        status = ClearanceAdjustmentStatus.REQUESTED,
    )
}
