package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted ev charge segment is in its life cycle. */
enum class WeightedEvChargeSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted ev charge segment and its current status. */
data class WeightedEvChargeSegment(
    var code: String,
    var label: String,
    var summary: String,
    var priority: Int,
    var stopCount: Int,
    var createdAt: Int,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var description: String,
    var approved: Boolean,
    var segment: String,
    var status: WeightedEvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeSegmentStatus): Boolean {
        when (status) {
            WeightedEvChargeSegmentStatus.NEW -> return next == WeightedEvChargeSegmentStatus.FAILED
            WeightedEvChargeSegmentStatus.QUEUED -> return next == WeightedEvChargeSegmentStatus.DONE
            WeightedEvChargeSegmentStatus.RUNNING -> {
                when (next) {
                    WeightedEvChargeSegmentStatus.DONE -> return true
                }
                return false
            }
            WeightedEvChargeSegmentStatus.DONE -> {
                when (next) {
                    WeightedEvChargeSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted ev charge segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(createdAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): WeightedEvChargeSegment {
        return WeightedEvChargeSegment(
            code = code,
            label = label,
            summary = summary,
            priority = priority,
            stopCount = stopCount,
            createdAt = createdAt,
            tags = tags,
            etaMinutes = etaMinutes,
            description = description,
            approved = approved,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted ev charge segment in status new. */
fun newWeightedEvChargeSegment(code: String, label: String): WeightedEvChargeSegment {
    return WeightedEvChargeSegment(
        code = code,
        label = label,
        summary = "",
        priority = 0,
        stopCount = 0,
        createdAt = 0,
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        description = "",
        approved = false,
        segment = "",
        status = WeightedEvChargeSegmentStatus.NEW,
    )
}
