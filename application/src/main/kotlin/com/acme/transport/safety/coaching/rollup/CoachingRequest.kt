package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching request is in its life cycle. */
enum class CoachingRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One coaching request, with what the planners need to act on it. */
data class CoachingRequest(
    var code: String,
    var title: String,
    var version: Int,
    var externalRef: String,
    var revision: Int,
    var budget: Double,
    var channel: String,
    var status: CoachingRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingRequestStatus): Boolean {
        when (status) {
            CoachingRequestStatus.REQUESTED -> return next == CoachingRequestStatus.REVOKED
            CoachingRequestStatus.GRANTED -> return next == CoachingRequestStatus.REVOKED || next == CoachingRequestStatus.EXPIRED
            CoachingRequestStatus.REVOKED -> {
                when (next) {
                    CoachingRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingRequestStatus) {
        if (!canMoveTo(next)) {
            error("coaching request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"coaching request"} $code"

    fun projectedVolume(): Double = round((budget + version.toDouble()) * 100) / 100

    fun snapshot(): CoachingRequest {
        return CoachingRequest(
            code = code,
            title = title,
            version = version,
            externalRef = externalRef,
            revision = revision,
            budget = budget,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a coaching request in status requested. */
fun newCoachingRequest(code: String, title: String): CoachingRequest {
    return CoachingRequest(
        code = code,
        title = title,
        version = 0,
        externalRef = "",
        revision = 0,
        budget = 0.0,
        channel = "",
        status = CoachingRequestStatus.REQUESTED,
    )
}
