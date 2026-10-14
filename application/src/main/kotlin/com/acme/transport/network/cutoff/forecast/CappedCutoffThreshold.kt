package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cutoff threshold is in its life cycle. */
enum class CappedCutoffThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped cutoff threshold. Its code is unique within a tenant. */
data class CappedCutoffThreshold(
    var code: String,
    var label: String,
    var driverId: String,
    var revision: Int,
    var etaMinutes: Int,
    var distanceKm: Double,
    var notes: String,
    var checksum: String,
    var trailerId: String,
    var pallets: Int,
    var status: CappedCutoffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCutoffThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCutoffThresholdStatus): Boolean {
        when (status) {
            CappedCutoffThresholdStatus.PLANNED -> return next == CappedCutoffThresholdStatus.SCHEDULED || next == CappedCutoffThresholdStatus.IN_PROGRESS
            CappedCutoffThresholdStatus.SCHEDULED -> {
                when (next) {
                    CappedCutoffThresholdStatus.COMPLETED, CappedCutoffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CappedCutoffThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    CappedCutoffThresholdStatus.COMPLETED, CappedCutoffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CappedCutoffThresholdStatus.COMPLETED -> {
                when (next) {
                    CappedCutoffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCutoffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped cutoff threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"capped cutoff threshold"} $code"

    fun adjustedValue(): Double = round((revision.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): CappedCutoffThreshold {
        return CappedCutoffThreshold(
            code = code,
            label = label,
            driverId = driverId,
            revision = revision,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            notes = notes,
            checksum = checksum,
            trailerId = trailerId,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped cutoff threshold in status planned. */
fun newCappedCutoffThreshold(code: String, label: String): CappedCutoffThreshold {
    return CappedCutoffThreshold(
        code = code,
        label = label,
        driverId = "",
        revision = 0,
        etaMinutes = 0,
        distanceKm = 0.0,
        notes = "",
        checksum = "",
        trailerId = "",
        pallets = 0,
        status = CappedCutoffThresholdStatus.PLANNED,
    )
}
