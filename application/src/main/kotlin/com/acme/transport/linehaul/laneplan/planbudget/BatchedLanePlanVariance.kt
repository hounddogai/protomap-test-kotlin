package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched lane plan variance is in its life cycle. */
enum class BatchedLanePlanVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched lane plan variance, with what the planners need to act on it. */
data class BatchedLanePlanVariance(
    var code: String,
    var label: String,
    var segment: String,
    var pallets: Int,
    var floorValue: Double,
    var graceDays: Int,
    var vehicleId: String,
    var weight: Double,
    var status: BatchedLanePlanVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLanePlanVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLanePlanVarianceStatus): Boolean {
        when (status) {
            BatchedLanePlanVarianceStatus.NEW -> {
                when (next) {
                    BatchedLanePlanVarianceStatus.DONE -> return true
                }
                return false
            }
            BatchedLanePlanVarianceStatus.QUEUED -> return next == BatchedLanePlanVarianceStatus.DONE
            BatchedLanePlanVarianceStatus.RUNNING -> {
                when (next) {
                    BatchedLanePlanVarianceStatus.FAILED -> return true
                }
                return false
            }
            BatchedLanePlanVarianceStatus.DONE -> {
                when (next) {
                    BatchedLanePlanVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLanePlanVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched lane plan variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((weight + floorValue) * 100) / 100

    fun snapshot(): BatchedLanePlanVariance {
        return BatchedLanePlanVariance(
            code = code,
            label = label,
            segment = segment,
            pallets = pallets,
            floorValue = floorValue,
            graceDays = graceDays,
            vehicleId = vehicleId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched lane plan variance in status new. */
fun newBatchedLanePlanVariance(code: String, label: String): BatchedLanePlanVariance {
    return BatchedLanePlanVariance(
        code = code,
        label = label,
        segment = "",
        pallets = 0,
        floorValue = 0.0,
        graceDays = 0,
        vehicleId = "",
        weight = 0.0,
        status = BatchedLanePlanVarianceStatus.NEW,
    )
}
