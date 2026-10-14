package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default emission factor snapshot is in its life cycle. */
enum class DefaultEmissionFactorSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default emission factor snapshot and its current status. */
data class DefaultEmissionFactorSnapshot(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var graceDays: Int,
    var odometer: Int,
    var summary: String,
    var etaMinutes: Int,
    var visible: Boolean,
    var unit: String,
    var status: DefaultEmissionFactorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEmissionFactorSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEmissionFactorSnapshotStatus): Boolean {
        when (status) {
            DefaultEmissionFactorSnapshotStatus.NEW -> {
                when (next) {
                    DefaultEmissionFactorSnapshotStatus.QUEUED -> return true
                }
                return false
            }
            DefaultEmissionFactorSnapshotStatus.QUEUED -> return next == DefaultEmissionFactorSnapshotStatus.DONE || next == DefaultEmissionFactorSnapshotStatus.FAILED
            DefaultEmissionFactorSnapshotStatus.RUNNING -> {
                when (next) {
                    DefaultEmissionFactorSnapshotStatus.DONE, DefaultEmissionFactorSnapshotStatus.FAILED -> return true
                }
                return false
            }
            DefaultEmissionFactorSnapshotStatus.DONE -> {
                when (next) {
                    DefaultEmissionFactorSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEmissionFactorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default emission factor snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((etaMinutes.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultEmissionFactorSnapshot {
        return DefaultEmissionFactorSnapshot(
            code = code,
            name = name,
            tags = tags,
            graceDays = graceDays,
            odometer = odometer,
            summary = summary,
            etaMinutes = etaMinutes,
            visible = visible,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default emission factor snapshot in status new. */
fun newDefaultEmissionFactorSnapshot(code: String, name: String): DefaultEmissionFactorSnapshot {
    return DefaultEmissionFactorSnapshot(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        graceDays = 0,
        odometer = 0,
        summary = "",
        etaMinutes = 0,
        visible = false,
        unit = "",
        status = DefaultEmissionFactorSnapshotStatus.NEW,
    )
}
