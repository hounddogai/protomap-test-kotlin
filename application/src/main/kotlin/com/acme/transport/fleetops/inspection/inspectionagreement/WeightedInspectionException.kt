package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection exception is in its life cycle. */
enum class WeightedInspectionExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted inspection exception and its current status. */
data class WeightedInspectionException(
    var code: String,
    var label: String,
    var vehicleId: String,
    var lane: String,
    var capacity: Int,
    var ceiling: Double,
    var graceDays: Int,
    var endsAt: Int,
    var minimum: Double,
    var discount: Double,
    var status: WeightedInspectionExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionExceptionStatus): Boolean {
        when (status) {
            WeightedInspectionExceptionStatus.NEW -> {
                when (next) {
                    WeightedInspectionExceptionStatus.QUEUED, WeightedInspectionExceptionStatus.FAILED -> return true
                }
                return false
            }
            WeightedInspectionExceptionStatus.QUEUED -> return next == WeightedInspectionExceptionStatus.DONE
            WeightedInspectionExceptionStatus.RUNNING -> return next == WeightedInspectionExceptionStatus.DONE
            WeightedInspectionExceptionStatus.DONE -> {
                when (next) {
                    WeightedInspectionExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedInspectionExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round(ceiling * capacity.toDouble() * 100) / 100

    fun snapshot(): WeightedInspectionException {
        return WeightedInspectionException(
            code = code,
            label = label,
            vehicleId = vehicleId,
            lane = lane,
            capacity = capacity,
            ceiling = ceiling,
            graceDays = graceDays,
            endsAt = endsAt,
            minimum = minimum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted inspection exception in status new. */
fun newWeightedInspectionException(code: String, label: String): WeightedInspectionException {
    return WeightedInspectionException(
        code = code,
        label = label,
        vehicleId = "",
        lane = "",
        capacity = 0,
        ceiling = 0.0,
        graceDays = 0,
        endsAt = 0,
        minimum = 0.0,
        discount = 0.0,
        status = WeightedInspectionExceptionStatus.NEW,
    )
}
