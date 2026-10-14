package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual defect rollup is in its life cycle. */
enum class ManualDefectRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual defect rollup and its current status. */
data class ManualDefectRollup(
    var code: String,
    var label: String,
    var graceDays: Int,
    var phone: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var segment: String,
    var active: Boolean,
    var priority: Int,
    var status: ManualDefectRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectRollupStatus): Boolean {
        when (status) {
            ManualDefectRollupStatus.IDLE -> {
                when (next) {
                    ManualDefectRollupStatus.STOPPED -> return true
                }
                return false
            }
            ManualDefectRollupStatus.WARMING -> {
                when (next) {
                    ManualDefectRollupStatus.READY -> return true
                }
                return false
            }
            ManualDefectRollupStatus.READY -> return next == ManualDefectRollupStatus.DRAINING || next == ManualDefectRollupStatus.STOPPED
            ManualDefectRollupStatus.DRAINING -> {
                when (next) {
                    ManualDefectRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDefectRollupStatus) {
        if (!canMoveTo(next)) {
            error("manual defect rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual defect rollup"} $code"

    fun netTotal(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ManualDefectRollup {
        return ManualDefectRollup(
            code = code,
            label = label,
            graceDays = graceDays,
            phone = phone,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            segment = segment,
            active = active,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual defect rollup in status idle. */
fun newManualDefectRollup(code: String, label: String): ManualDefectRollup {
    return ManualDefectRollup(
        code = code,
        label = label,
        graceDays = 0,
        phone = "",
        loadKg = 0.0,
        durationMinutes = 0,
        segment = "",
        active = false,
        priority = 0,
        status = ManualDefectRollupStatus.IDLE,
    )
}
