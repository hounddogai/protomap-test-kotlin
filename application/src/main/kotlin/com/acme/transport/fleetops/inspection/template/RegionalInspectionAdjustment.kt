package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional inspection adjustment is in its life cycle. */
enum class RegionalInspectionAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional inspection adjustment, with what the planners need to act on it. */
data class RegionalInspectionAdjustment(
    var code: String,
    var name: String,
    var retries: Int,
    var trailerId: String,
    var lane: String,
    var externalRef: String,
    var unit: String,
    var vehicleId: String,
    var ratio: Double,
    var status: RegionalInspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionAdjustmentStatus): Boolean {
        when (status) {
            RegionalInspectionAdjustmentStatus.NEW -> {
                when (next) {
                    RegionalInspectionAdjustmentStatus.DONE -> return true
                }
                return false
            }
            RegionalInspectionAdjustmentStatus.QUEUED -> return next == RegionalInspectionAdjustmentStatus.DONE
            RegionalInspectionAdjustmentStatus.RUNNING -> {
                when (next) {
                    RegionalInspectionAdjustmentStatus.DONE, RegionalInspectionAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            RegionalInspectionAdjustmentStatus.DONE -> {
                when (next) {
                    RegionalInspectionAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalInspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional inspection adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional inspection adjustment"} $code"

    fun effectiveVolume(): Double = round((ratio + retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalInspectionAdjustment {
        return RegionalInspectionAdjustment(
            code = code,
            name = name,
            retries = retries,
            trailerId = trailerId,
            lane = lane,
            externalRef = externalRef,
            unit = unit,
            vehicleId = vehicleId,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional inspection adjustment in status new. */
fun newRegionalInspectionAdjustment(code: String, name: String): RegionalInspectionAdjustment {
    return RegionalInspectionAdjustment(
        code = code,
        name = name,
        retries = 0,
        trailerId = "",
        lane = "",
        externalRef = "",
        unit = "",
        vehicleId = "",
        ratio = 0.0,
        status = RegionalInspectionAdjustmentStatus.NEW,
    )
}
