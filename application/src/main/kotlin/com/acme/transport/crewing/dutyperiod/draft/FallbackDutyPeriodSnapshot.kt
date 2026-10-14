package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback duty period snapshot is in its life cycle. */
enum class FallbackDutyPeriodSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback duty period snapshot as the duty period team keeps it. */
data class FallbackDutyPeriodSnapshot(
    var code: String,
    var label: String,
    var margin: Double,
    var lane: String,
    var loadKg: Double,
    var title: String,
    var status: FallbackDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDutyPeriodSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            FallbackDutyPeriodSnapshotStatus.NEW -> return next == FallbackDutyPeriodSnapshotStatus.RUNNING
            FallbackDutyPeriodSnapshotStatus.QUEUED -> {
                when (next) {
                    FallbackDutyPeriodSnapshotStatus.FAILED -> return true
                }
                return false
            }
            FallbackDutyPeriodSnapshotStatus.RUNNING -> {
                when (next) {
                    FallbackDutyPeriodSnapshotStatus.FAILED -> return true
                }
                return false
            }
            FallbackDutyPeriodSnapshotStatus.DONE -> {
                when (next) {
                    FallbackDutyPeriodSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fallback duty period snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round(margin * loadKg * 100) / 100

    fun snapshot(): FallbackDutyPeriodSnapshot {
        return FallbackDutyPeriodSnapshot(
            code = code,
            label = label,
            margin = margin,
            lane = lane,
            loadKg = loadKg,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback duty period snapshot in status new. */
fun newFallbackDutyPeriodSnapshot(code: String, label: String): FallbackDutyPeriodSnapshot {
    return FallbackDutyPeriodSnapshot(
        code = code,
        label = label,
        margin = 0.0,
        lane = "",
        loadKg = 0.0,
        title = "",
        status = FallbackDutyPeriodSnapshotStatus.NEW,
    )
}
