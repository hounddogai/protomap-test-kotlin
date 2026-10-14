package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection bundle is in its life cycle. */
enum class WeightedInspectionBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted inspection bundle as the inspection team keeps it. */
data class WeightedInspectionBundle(
    var code: String,
    var title: String,
    var notes: String,
    var lane: String,
    var sequence: Int,
    var cost: Double,
    var createdAt: Int,
    var shiftCode: String,
    var etaMinutes: Int,
    var tags: MutableList<String>,
    var status: WeightedInspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionBundleStatus): Boolean {
        when (status) {
            WeightedInspectionBundleStatus.OPEN -> {
                when (next) {
                    WeightedInspectionBundleStatus.RESOLVED -> return true
                }
                return false
            }
            WeightedInspectionBundleStatus.IN_REVIEW -> {
                when (next) {
                    WeightedInspectionBundleStatus.REOPENED -> return true
                }
                return false
            }
            WeightedInspectionBundleStatus.RESOLVED -> {
                when (next) {
                    WeightedInspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedInspectionBundleStatus.REOPENED -> {
                when (next) {
                    WeightedInspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedInspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((sequence.toDouble() - cost) * 100) / 100

    fun snapshot(): WeightedInspectionBundle {
        return WeightedInspectionBundle(
            code = code,
            title = title,
            notes = notes,
            lane = lane,
            sequence = sequence,
            cost = cost,
            createdAt = createdAt,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted inspection bundle in status open. */
fun newWeightedInspectionBundle(code: String, title: String): WeightedInspectionBundle {
    return WeightedInspectionBundle(
        code = code,
        title = title,
        notes = "",
        lane = "",
        sequence = 0,
        cost = 0.0,
        createdAt = 0,
        shiftCode = "",
        etaMinutes = 0,
        tags = mutableListOf<String>(),
        status = WeightedInspectionBundleStatus.OPEN,
    )
}
