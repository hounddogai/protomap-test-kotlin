package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell adjustment is in its life cycle. */
enum class DwellAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One dwell adjustment, with what the planners need to act on it. */
data class DwellAdjustment(
    var code: String,
    var name: String,
    var threshold: Double,
    var summary: String,
    var priority: Int,
    var status: DwellAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellAdjustmentStatus): Boolean {
        when (status) {
            DwellAdjustmentStatus.CREATED -> return next == DwellAdjustmentStatus.VALIDATED || next == DwellAdjustmentStatus.PUBLISHED
            DwellAdjustmentStatus.VALIDATED -> return next == DwellAdjustmentStatus.PUBLISHED
            DwellAdjustmentStatus.PUBLISHED -> return next == DwellAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DwellAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("dwell adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((priority.toDouble() - threshold) * 100) / 100

    fun snapshot(): DwellAdjustment {
        return DwellAdjustment(
            code = code,
            name = name,
            threshold = threshold,
            summary = summary,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dwell adjustment in status created. */
fun newDwellAdjustment(code: String, name: String): DwellAdjustment {
    return DwellAdjustment(
        code = code,
        name = name,
        threshold = 0.0,
        summary = "",
        priority = 0,
        status = DwellAdjustmentStatus.CREATED,
    )
}
