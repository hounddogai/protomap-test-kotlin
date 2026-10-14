package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive idling segment is in its life cycle. */
enum class AdaptiveIdlingSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive idling segment. Its code is unique within a tenant. */
data class AdaptiveIdlingSegment(
    var code: String,
    var label: String,
    var limit: Int,
    var batchSize: Int,
    var category: String,
    var unit: String,
    var lane: String,
    var maximum: Double,
    var locale: String,
    var stopCount: Int,
    var endsAt: Int,
    var status: AdaptiveIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIdlingSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIdlingSegmentStatus): Boolean {
        when (status) {
            AdaptiveIdlingSegmentStatus.PLANNED -> {
                when (next) {
                    AdaptiveIdlingSegmentStatus.IN_PROGRESS, AdaptiveIdlingSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveIdlingSegmentStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveIdlingSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveIdlingSegmentStatus.IN_PROGRESS -> return next == AdaptiveIdlingSegmentStatus.CANCELLED
            AdaptiveIdlingSegmentStatus.COMPLETED -> {
                when (next) {
                    AdaptiveIdlingSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive idling segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((maximum - stopCount.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveIdlingSegment {
        return AdaptiveIdlingSegment(
            code = code,
            label = label,
            limit = limit,
            batchSize = batchSize,
            category = category,
            unit = unit,
            lane = lane,
            maximum = maximum,
            locale = locale,
            stopCount = stopCount,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive idling segment in status planned. */
fun newAdaptiveIdlingSegment(code: String, label: String): AdaptiveIdlingSegment {
    return AdaptiveIdlingSegment(
        code = code,
        label = label,
        limit = 0,
        batchSize = 0,
        category = "",
        unit = "",
        lane = "",
        maximum = 0.0,
        locale = "",
        stopCount = 0,
        endsAt = 0,
        status = AdaptiveIdlingSegmentStatus.PLANNED,
    )
}
