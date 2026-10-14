package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization record records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization record is in its life cycle. */
enum class UtilizationRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One utilization record, with what the planners need to act on it. */
data class UtilizationRecord(
    var code: String,
    var label: String,
    var limit: Int,
    var stopCount: Int,
    var fuelLiters: Double,
    var discount: Double,
    var vehicleId: String,
    var externalRef: String,
    var batchSize: Int,
    var status: UtilizationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationRecordStatus): Boolean {
        when (status) {
            UtilizationRecordStatus.NEW -> {
                when (next) {
                    UtilizationRecordStatus.RUNNING -> return true
                }
                return false
            }
            UtilizationRecordStatus.QUEUED -> {
                when (next) {
                    UtilizationRecordStatus.DONE -> return true
                }
                return false
            }
            UtilizationRecordStatus.RUNNING -> {
                when (next) {
                    UtilizationRecordStatus.DONE -> return true
                }
                return false
            }
            UtilizationRecordStatus.DONE -> return next == UtilizationRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: UtilizationRecordStatus) {
        if (!canMoveTo(next)) {
            error("utilization record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round(stopCount.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): UtilizationRecord {
        return UtilizationRecord(
            code = code,
            label = label,
            limit = limit,
            stopCount = stopCount,
            fuelLiters = fuelLiters,
            discount = discount,
            vehicleId = vehicleId,
            externalRef = externalRef,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization record in status new. */
fun newUtilizationRecord(code: String, label: String): UtilizationRecord {
    return UtilizationRecord(
        code = code,
        label = label,
        limit = 0,
        stopCount = 0,
        fuelLiters = 0.0,
        discount = 0.0,
        vehicleId = "",
        externalRef = "",
        batchSize = 0,
        status = UtilizationRecordStatus.NEW,
    )
}
