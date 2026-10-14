package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor batch records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor batch is in its life cycle. */
enum class EmissionFactorBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One emission factor batch, with what the planners need to act on it. */
data class EmissionFactorBatch(
    var code: String,
    var label: String,
    var region: String,
    var notes: String,
    var floorValue: Double,
    var capacity: Int,
    var dock: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var status: EmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorBatchStatus): Boolean {
        when (status) {
            EmissionFactorBatchStatus.IDLE -> return next == EmissionFactorBatchStatus.READY || next == EmissionFactorBatchStatus.STOPPED
            EmissionFactorBatchStatus.WARMING -> {
                when (next) {
                    EmissionFactorBatchStatus.READY, EmissionFactorBatchStatus.DRAINING -> return true
                }
                return false
            }
            EmissionFactorBatchStatus.READY -> return next == EmissionFactorBatchStatus.DRAINING
            EmissionFactorBatchStatus.DRAINING -> return next == EmissionFactorBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: EmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("emission factor batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((floorValue - loadKg) * 100) / 100

    fun snapshot(): EmissionFactorBatch {
        return EmissionFactorBatch(
            code = code,
            label = label,
            region = region,
            notes = notes,
            floorValue = floorValue,
            capacity = capacity,
            dock = dock,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a emission factor batch in status idle. */
fun newEmissionFactorBatch(code: String, label: String): EmissionFactorBatch {
    return EmissionFactorBatch(
        code = code,
        label = label,
        region = "",
        notes = "",
        floorValue = 0.0,
        capacity = 0,
        dock = "",
        durationMinutes = 0,
        loadKg = 0.0,
        status = EmissionFactorBatchStatus.IDLE,
    )
}
