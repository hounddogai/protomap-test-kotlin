package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor snapshot is in its life cycle. */
enum class EmissionFactorSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One emission factor snapshot, with what the planners need to act on it. */
data class EmissionFactorSnapshot(
    var code: String,
    var name: String,
    var channel: String,
    var cost: Double,
    var dock: String,
    var graceDays: Int,
    var odometer: Int,
    var loadKg: Double,
    var description: String,
    var status: EmissionFactorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorSnapshotStatus): Boolean {
        when (status) {
            EmissionFactorSnapshotStatus.NEW -> return next == EmissionFactorSnapshotStatus.QUEUED
            EmissionFactorSnapshotStatus.QUEUED -> {
                when (next) {
                    EmissionFactorSnapshotStatus.FAILED -> return true
                }
                return false
            }
            EmissionFactorSnapshotStatus.RUNNING -> return next == EmissionFactorSnapshotStatus.DONE
            EmissionFactorSnapshotStatus.DONE -> return next == EmissionFactorSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("emission factor snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorSnapshot {
        return EmissionFactorSnapshot(
            code = code,
            name = name,
            channel = channel,
            cost = cost,
            dock = dock,
            graceDays = graceDays,
            odometer = odometer,
            loadKg = loadKg,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a emission factor snapshot in status new. */
fun newEmissionFactorSnapshot(code: String, name: String): EmissionFactorSnapshot {
    return EmissionFactorSnapshot(
        code = code,
        name = name,
        channel = "",
        cost = 0.0,
        dock = "",
        graceDays = 0,
        odometer = 0,
        loadKg = 0.0,
        description = "",
        status = EmissionFactorSnapshotStatus.NEW,
    )
}
