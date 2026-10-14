package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback idling segment is in its life cycle. */
enum class FallbackIdlingSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback idling segment and its current status. */
data class FallbackIdlingSegment(
    var code: String,
    var title: String,
    var discount: Double,
    var minimum: Double,
    var ceiling: Double,
    var approved: Boolean,
    var distanceKm: Double,
    var status: FallbackIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIdlingSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIdlingSegmentStatus): Boolean {
        when (status) {
            FallbackIdlingSegmentStatus.NEW -> return next == FallbackIdlingSegmentStatus.DONE
            FallbackIdlingSegmentStatus.QUEUED -> {
                when (next) {
                    FallbackIdlingSegmentStatus.DONE -> return true
                }
                return false
            }
            FallbackIdlingSegmentStatus.RUNNING -> {
                when (next) {
                    FallbackIdlingSegmentStatus.FAILED -> return true
                }
                return false
            }
            FallbackIdlingSegmentStatus.DONE -> {
                when (next) {
                    FallbackIdlingSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback idling segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback idling segment"} $code"

    fun netVolume(): Double = round(discount * ceiling * 100) / 100

    fun snapshot(): FallbackIdlingSegment {
        return FallbackIdlingSegment(
            code = code,
            title = title,
            discount = discount,
            minimum = minimum,
            ceiling = ceiling,
            approved = approved,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback idling segment in status new. */
fun newFallbackIdlingSegment(code: String, title: String): FallbackIdlingSegment {
    return FallbackIdlingSegment(
        code = code,
        title = title,
        discount = 0.0,
        minimum = 0.0,
        ceiling = 0.0,
        approved = false,
        distanceKm = 0.0,
        status = FallbackIdlingSegmentStatus.NEW,
    )
}
