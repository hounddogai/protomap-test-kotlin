package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dispute segment is in its life cycle. */
enum class SeasonalDisputeSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal dispute segment and its current status. */
data class SeasonalDisputeSegment(
    var code: String,
    var title: String,
    var notes: String,
    var ratio: Double,
    var shiftCode: String,
    var stopCount: Int,
    var distanceKm: Double,
    var name: String,
    var priority: Int,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var status: SeasonalDisputeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDisputeSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDisputeSegmentStatus): Boolean {
        when (status) {
            SeasonalDisputeSegmentStatus.PLANNED -> return next == SeasonalDisputeSegmentStatus.IN_PROGRESS || next == SeasonalDisputeSegmentStatus.CANCELLED
            SeasonalDisputeSegmentStatus.SCHEDULED -> return next == SeasonalDisputeSegmentStatus.IN_PROGRESS
            SeasonalDisputeSegmentStatus.IN_PROGRESS -> return next == SeasonalDisputeSegmentStatus.CANCELLED
            SeasonalDisputeSegmentStatus.COMPLETED -> return next == SeasonalDisputeSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalDisputeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dispute segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal dispute segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(stopCount.toDouble() * ratio * 100) / 100

    fun snapshot(): SeasonalDisputeSegment {
        return SeasonalDisputeSegment(
            code = code,
            title = title,
            notes = notes,
            ratio = ratio,
            shiftCode = shiftCode,
            stopCount = stopCount,
            distanceKm = distanceKm,
            name = name,
            priority = priority,
            batchSize = batchSize,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal dispute segment in status planned. */
fun newSeasonalDisputeSegment(code: String, title: String): SeasonalDisputeSegment {
    return SeasonalDisputeSegment(
        code = code,
        title = title,
        notes = "",
        ratio = 0.0,
        shiftCode = "",
        stopCount = 0,
        distanceKm = 0.0,
        name = "",
        priority = 0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        status = SeasonalDisputeSegmentStatus.PLANNED,
    )
}
