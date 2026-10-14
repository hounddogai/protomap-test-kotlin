package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection window is in its life cycle. */
enum class PrimaryInspectionWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary inspection window as the inspection team keeps it. */
data class PrimaryInspectionWindow(
    var code: String,
    var title: String,
    var capacity: Int,
    var ratio: Double,
    var active: Boolean,
    var source: String,
    var status: PrimaryInspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionWindowStatus): Boolean {
        when (status) {
            PrimaryInspectionWindowStatus.NEW -> return next == PrimaryInspectionWindowStatus.QUEUED
            PrimaryInspectionWindowStatus.QUEUED -> return next == PrimaryInspectionWindowStatus.RUNNING
            PrimaryInspectionWindowStatus.RUNNING -> return next == PrimaryInspectionWindowStatus.DONE || next == PrimaryInspectionWindowStatus.FAILED
            PrimaryInspectionWindowStatus.DONE -> {
                when (next) {
                    PrimaryInspectionWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((capacity.toDouble() - ratio) * 100) / 100

    fun snapshot(): PrimaryInspectionWindow {
        return PrimaryInspectionWindow(
            code = code,
            title = title,
            capacity = capacity,
            ratio = ratio,
            active = active,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary inspection window in status new. */
fun newPrimaryInspectionWindow(code: String, title: String): PrimaryInspectionWindow {
    return PrimaryInspectionWindow(
        code = code,
        title = title,
        capacity = 0,
        ratio = 0.0,
        active = false,
        source = "",
        status = PrimaryInspectionWindowStatus.NEW,
    )
}
