package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted hold segment records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a weighted hold segment is in its life cycle. */
enum class WeightedHoldSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted hold segment. Its code is unique within a tenant. */
data class WeightedHoldSegment(
    var code: String,
    var title: String,
    var batchSize: Int,
    var lane: String,
    var stopCount: Int,
    var cost: Double,
    var durationMinutes: Int,
    var etaMinutes: Int,
    var phone: String,
    var locale: String,
    var archived: Boolean,
    var status: WeightedHoldSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHoldSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHoldSegmentStatus): Boolean {
        when (status) {
            WeightedHoldSegmentStatus.REQUESTED -> return next == WeightedHoldSegmentStatus.GRANTED || next == WeightedHoldSegmentStatus.EXPIRED
            WeightedHoldSegmentStatus.GRANTED -> {
                when (next) {
                    WeightedHoldSegmentStatus.REVOKED, WeightedHoldSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedHoldSegmentStatus.REVOKED -> {
                when (next) {
                    WeightedHoldSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHoldSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted hold segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted hold segment"} $code"

    fun weightedValue(): Double = round((batchSize.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedHoldSegment {
        return WeightedHoldSegment(
            code = code,
            title = title,
            batchSize = batchSize,
            lane = lane,
            stopCount = stopCount,
            cost = cost,
            durationMinutes = durationMinutes,
            etaMinutes = etaMinutes,
            phone = phone,
            locale = locale,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted hold segment in status requested. */
fun newWeightedHoldSegment(code: String, title: String): WeightedHoldSegment {
    return WeightedHoldSegment(
        code = code,
        title = title,
        batchSize = 0,
        lane = "",
        stopCount = 0,
        cost = 0.0,
        durationMinutes = 0,
        etaMinutes = 0,
        phone = "",
        locale = "",
        archived = false,
        status = WeightedHoldSegmentStatus.REQUESTED,
    )
}
