package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dwell profile is in its life cycle. */
enum class ManualDwellProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual dwell profile and its current status. */
data class ManualDwellProfile(
    var code: String,
    var title: String,
    var margin: Double,
    var floorValue: Double,
    var summary: String,
    var durationMinutes: Int,
    var segment: String,
    var odometer: Int,
    var unit: String,
    var ceiling: Double,
    var status: ManualDwellProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellProfileStatus): Boolean {
        when (status) {
            ManualDwellProfileStatus.NEW -> return next == ManualDwellProfileStatus.QUEUED
            ManualDwellProfileStatus.QUEUED -> {
                when (next) {
                    ManualDwellProfileStatus.DONE, ManualDwellProfileStatus.FAILED -> return true
                }
                return false
            }
            ManualDwellProfileStatus.RUNNING -> {
                when (next) {
                    ManualDwellProfileStatus.DONE -> return true
                }
                return false
            }
            ManualDwellProfileStatus.DONE -> return next == ManualDwellProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualDwellProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round((durationMinutes.toDouble() - margin) * 100) / 100

    fun snapshot(): ManualDwellProfile {
        return ManualDwellProfile(
            code = code,
            title = title,
            margin = margin,
            floorValue = floorValue,
            summary = summary,
            durationMinutes = durationMinutes,
            segment = segment,
            odometer = odometer,
            unit = unit,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual dwell profile in status new. */
fun newManualDwellProfile(code: String, title: String): ManualDwellProfile {
    return ManualDwellProfile(
        code = code,
        title = title,
        margin = 0.0,
        floorValue = 0.0,
        summary = "",
        durationMinutes = 0,
        segment = "",
        odometer = 0,
        unit = "",
        ceiling = 0.0,
        status = ManualDwellProfileStatus.NEW,
    )
}
