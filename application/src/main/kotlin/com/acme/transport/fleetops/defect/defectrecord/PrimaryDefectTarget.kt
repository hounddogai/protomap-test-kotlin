package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary defect target is in its life cycle. */
enum class PrimaryDefectTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary defect target and its current status. */
data class PrimaryDefectTarget(
    var code: String,
    var label: String,
    var floorValue: Double,
    var maximum: Double,
    var createdAt: Int,
    var expiresAt: Int,
    var shiftCode: String,
    var odometer: Int,
    var lane: String,
    var category: String,
    var status: PrimaryDefectTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDefectTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDefectTargetStatus): Boolean {
        when (status) {
            PrimaryDefectTargetStatus.REQUESTED -> {
                when (next) {
                    PrimaryDefectTargetStatus.GRANTED, PrimaryDefectTargetStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryDefectTargetStatus.GRANTED -> return next == PrimaryDefectTargetStatus.REVOKED
            PrimaryDefectTargetStatus.REVOKED -> {
                when (next) {
                    PrimaryDefectTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDefectTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary defect target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round(createdAt.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): PrimaryDefectTarget {
        return PrimaryDefectTarget(
            code = code,
            label = label,
            floorValue = floorValue,
            maximum = maximum,
            createdAt = createdAt,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            odometer = odometer,
            lane = lane,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary defect target in status requested. */
fun newPrimaryDefectTarget(code: String, label: String): PrimaryDefectTarget {
    return PrimaryDefectTarget(
        code = code,
        label = label,
        floorValue = 0.0,
        maximum = 0.0,
        createdAt = 0,
        expiresAt = 0,
        shiftCode = "",
        odometer = 0,
        lane = "",
        category = "",
        status = PrimaryDefectTargetStatus.REQUESTED,
    )
}
