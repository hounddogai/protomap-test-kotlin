package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time batch records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time batch is in its life cycle. */
enum class TransitTimeBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a transit time batch and its current status. */
data class TransitTimeBatch(
    var code: String,
    var name: String,
    var vehicleId: String,
    var loadKg: Double,
    var odometer: Int,
    var sequence: Int,
    var status: TransitTimeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeBatchStatus): Boolean {
        when (status) {
            TransitTimeBatchStatus.NEW -> return next == TransitTimeBatchStatus.RUNNING
            TransitTimeBatchStatus.QUEUED -> return next == TransitTimeBatchStatus.RUNNING
            TransitTimeBatchStatus.RUNNING -> {
                when (next) {
                    TransitTimeBatchStatus.DONE -> return true
                }
                return false
            }
            TransitTimeBatchStatus.DONE -> {
                when (next) {
                    TransitTimeBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeBatchStatus) {
        if (!canMoveTo(next)) {
            error("transit time batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((odometer.toDouble() + loadKg) * 100) / 100

    fun snapshot(): TransitTimeBatch {
        return TransitTimeBatch(
            code = code,
            name = name,
            vehicleId = vehicleId,
            loadKg = loadKg,
            odometer = odometer,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time batch in status new. */
fun newTransitTimeBatch(code: String, name: String): TransitTimeBatch {
    return TransitTimeBatch(
        code = code,
        name = name,
        vehicleId = "",
        loadKg = 0.0,
        odometer = 0,
        sequence = 0,
        status = TransitTimeBatchStatus.NEW,
    )
}
