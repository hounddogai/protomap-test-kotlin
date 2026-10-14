package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel burn threshold is in its life cycle. */
enum class PendingFuelBurnThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending fuel burn threshold and its current status. */
data class PendingFuelBurnThreshold(
    var code: String,
    var label: String,
    var loadKg: Double,
    var score: Double,
    var attempts: Int,
    var notes: String,
    var region: String,
    var status: PendingFuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelBurnThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelBurnThresholdStatus): Boolean {
        when (status) {
            PendingFuelBurnThresholdStatus.NEW -> return next == PendingFuelBurnThresholdStatus.RUNNING || next == PendingFuelBurnThresholdStatus.DONE
            PendingFuelBurnThresholdStatus.QUEUED -> return next == PendingFuelBurnThresholdStatus.RUNNING || next == PendingFuelBurnThresholdStatus.DONE
            PendingFuelBurnThresholdStatus.RUNNING -> return next == PendingFuelBurnThresholdStatus.FAILED
            PendingFuelBurnThresholdStatus.DONE -> return next == PendingFuelBurnThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingFuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending fuel burn threshold"} $code"

    fun projectedValue(): Double = round(loadKg * score * 100) / 100

    fun snapshot(): PendingFuelBurnThreshold {
        return PendingFuelBurnThreshold(
            code = code,
            label = label,
            loadKg = loadKg,
            score = score,
            attempts = attempts,
            notes = notes,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending fuel burn threshold in status new. */
fun newPendingFuelBurnThreshold(code: String, label: String): PendingFuelBurnThreshold {
    return PendingFuelBurnThreshold(
        code = code,
        label = label,
        loadKg = 0.0,
        score = 0.0,
        attempts = 0,
        notes = "",
        region = "",
        status = PendingFuelBurnThresholdStatus.NEW,
    )
}
