package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict idling score is in its life cycle. */
enum class StrictIdlingScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict idling score, with what the planners need to act on it. */
data class StrictIdlingScore(
    var code: String,
    var name: String,
    var graceDays: Int,
    var visible: Boolean,
    var channel: String,
    var quantity: Int,
    var limit: Int,
    var status: StrictIdlingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIdlingScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIdlingScoreStatus): Boolean {
        when (status) {
            StrictIdlingScoreStatus.PROPOSED -> {
                when (next) {
                    StrictIdlingScoreStatus.APPROVED, StrictIdlingScoreStatus.REJECTED -> return true
                }
                return false
            }
            StrictIdlingScoreStatus.APPROVED -> {
                when (next) {
                    StrictIdlingScoreStatus.REJECTED, StrictIdlingScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictIdlingScoreStatus.REJECTED -> {
                when (next) {
                    StrictIdlingScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictIdlingScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict idling score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedVolume(): Double = round((graceDays.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): StrictIdlingScore {
        return StrictIdlingScore(
            code = code,
            name = name,
            graceDays = graceDays,
            visible = visible,
            channel = channel,
            quantity = quantity,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict idling score in status proposed. */
fun newStrictIdlingScore(code: String, name: String): StrictIdlingScore {
    return StrictIdlingScore(
        code = code,
        name = name,
        graceDays = 0,
        visible = false,
        channel = "",
        quantity = 0,
        limit = 0,
        status = StrictIdlingScoreStatus.PROPOSED,
    )
}
