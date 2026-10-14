package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional incident review is in its life cycle. */
enum class RegionalIncidentReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional incident review, with what the planners need to act on it. */
data class RegionalIncidentReview(
    var code: String,
    var name: String,
    var description: String,
    var startsAt: Int,
    var discount: Double,
    var vehicleId: String,
    var status: RegionalIncidentReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentReviewStatus): Boolean {
        when (status) {
            RegionalIncidentReviewStatus.IDLE -> {
                when (next) {
                    RegionalIncidentReviewStatus.DRAINING -> return true
                }
                return false
            }
            RegionalIncidentReviewStatus.WARMING -> return next == RegionalIncidentReviewStatus.READY
            RegionalIncidentReviewStatus.READY -> return next == RegionalIncidentReviewStatus.DRAINING
            RegionalIncidentReviewStatus.DRAINING -> {
                when (next) {
                    RegionalIncidentReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIncidentReviewStatus) {
        if (!canMoveTo(next)) {
            error("regional incident review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round(startsAt.toDouble() * discount * 100) / 100

    fun snapshot(): RegionalIncidentReview {
        return RegionalIncidentReview(
            code = code,
            name = name,
            description = description,
            startsAt = startsAt,
            discount = discount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional incident review in status idle. */
fun newRegionalIncidentReview(code: String, name: String): RegionalIncidentReview {
    return RegionalIncidentReview(
        code = code,
        name = name,
        description = "",
        startsAt = 0,
        discount = 0.0,
        vehicleId = "",
        status = RegionalIncidentReviewStatus.IDLE,
    )
}
