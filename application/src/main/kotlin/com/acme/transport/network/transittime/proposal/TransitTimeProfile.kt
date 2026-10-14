package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time profile is in its life cycle. */
enum class TransitTimeProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One transit time profile, with what the planners need to act on it. */
data class TransitTimeProfile(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var graceDays: Int,
    var lane: String,
    var status: TransitTimeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeProfileStatus): Boolean {
        when (status) {
            TransitTimeProfileStatus.REQUESTED -> return next == TransitTimeProfileStatus.GRANTED || next == TransitTimeProfileStatus.REVOKED
            TransitTimeProfileStatus.GRANTED -> {
                when (next) {
                    TransitTimeProfileStatus.REVOKED, TransitTimeProfileStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeProfileStatus.REVOKED -> return next == TransitTimeProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeProfileStatus) {
        if (!canMoveTo(next)) {
            error("transit time profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((durationMinutes.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeProfile {
        return TransitTimeProfile(
            code = code,
            name = name,
            tags = tags,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a transit time profile in status requested. */
fun newTransitTimeProfile(code: String, name: String): TransitTimeProfile {
    return TransitTimeProfile(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        graceDays = 0,
        lane = "",
        status = TransitTimeProfileStatus.REQUESTED,
    )
}
