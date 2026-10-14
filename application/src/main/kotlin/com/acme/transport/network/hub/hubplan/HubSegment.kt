package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub segment is in its life cycle. */
enum class HubSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hub segment, with what the planners need to act on it. */
data class HubSegment(
    var code: String,
    var title: String,
    var currency: String,
    var rate: Double,
    var contactEmail: String,
    var updatedAt: Int,
    var archived: Boolean,
    var threshold: Double,
    var status: HubSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubSegmentStatus): Boolean {
        when (status) {
            HubSegmentStatus.PLANNED -> return next == HubSegmentStatus.COMPLETED
            HubSegmentStatus.SCHEDULED -> {
                when (next) {
                    HubSegmentStatus.IN_PROGRESS, HubSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            HubSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    HubSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            HubSegmentStatus.COMPLETED -> return next == HubSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HubSegmentStatus) {
        if (!canMoveTo(next)) {
            error("hub segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((rate - threshold) * 100) / 100

    fun snapshot(): HubSegment {
        return HubSegment(
            code = code,
            title = title,
            currency = currency,
            rate = rate,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            archived = archived,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub segment in status planned. */
fun newHubSegment(code: String, title: String): HubSegment {
    return HubSegment(
        code = code,
        title = title,
        currency = "",
        rate = 0.0,
        contactEmail = "",
        updatedAt = 0,
        archived = false,
        threshold = 0.0,
        status = HubSegmentStatus.PLANNED,
    )
}
