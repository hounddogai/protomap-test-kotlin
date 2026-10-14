package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary transit time segment is in its life cycle. */
enum class PrimaryTransitTimeSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary transit time segment and its current status. */
data class PrimaryTransitTimeSegment(
    var code: String,
    var label: String,
    var ownerId: String,
    var etaMinutes: Int,
    var budget: Double,
    var source: String,
    var pinned: Boolean,
    var archived: Boolean,
    var revision: Int,
    var description: String,
    var floorValue: Double,
    var status: PrimaryTransitTimeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeSegmentStatus): Boolean {
        when (status) {
            PrimaryTransitTimeSegmentStatus.PLANNED -> return next == PrimaryTransitTimeSegmentStatus.CANCELLED
            PrimaryTransitTimeSegmentStatus.SCHEDULED -> {
                when (next) {
                    PrimaryTransitTimeSegmentStatus.COMPLETED, PrimaryTransitTimeSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryTransitTimeSegmentStatus.IN_PROGRESS -> return next == PrimaryTransitTimeSegmentStatus.COMPLETED
            PrimaryTransitTimeSegmentStatus.COMPLETED -> {
                when (next) {
                    PrimaryTransitTimeSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((floorValue - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTransitTimeSegment {
        return PrimaryTransitTimeSegment(
            code = code,
            label = label,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            budget = budget,
            source = source,
            pinned = pinned,
            archived = archived,
            revision = revision,
            description = description,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary transit time segment in status planned. */
fun newPrimaryTransitTimeSegment(code: String, label: String): PrimaryTransitTimeSegment {
    return PrimaryTransitTimeSegment(
        code = code,
        label = label,
        ownerId = "",
        etaMinutes = 0,
        budget = 0.0,
        source = "",
        pinned = false,
        archived = false,
        revision = 0,
        description = "",
        floorValue = 0.0,
        status = PrimaryTransitTimeSegmentStatus.PLANNED,
    )
}
