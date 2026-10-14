package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted defect segment is in its life cycle. */
enum class WeightedDefectSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted defect segment, with what the planners need to act on it. */
data class WeightedDefectSegment(
    var code: String,
    var label: String,
    var region: String,
    var windowDays: Int,
    var etaMinutes: Int,
    var status: WeightedDefectSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDefectSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDefectSegmentStatus): Boolean {
        when (status) {
            WeightedDefectSegmentStatus.DRAFT -> return next == WeightedDefectSegmentStatus.SUSPENDED || next == WeightedDefectSegmentStatus.CLOSED
            WeightedDefectSegmentStatus.PENDING -> return next == WeightedDefectSegmentStatus.CLOSED
            WeightedDefectSegmentStatus.ACTIVE -> return next == WeightedDefectSegmentStatus.SUSPENDED || next == WeightedDefectSegmentStatus.CLOSED
            WeightedDefectSegmentStatus.SUSPENDED -> {
                when (next) {
                    WeightedDefectSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDefectSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted defect segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted defect segment"} $code"

    fun weightedValue(): Double = round((etaMinutes.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): WeightedDefectSegment {
        return WeightedDefectSegment(
            code = code,
            label = label,
            region = region,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted defect segment in status draft. */
fun newWeightedDefectSegment(code: String, label: String): WeightedDefectSegment {
    return WeightedDefectSegment(
        code = code,
        label = label,
        region = "",
        windowDays = 0,
        etaMinutes = 0,
        status = WeightedDefectSegmentStatus.DRAFT,
    )
}
