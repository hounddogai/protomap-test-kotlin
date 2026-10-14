package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time score is in its life cycle. */
enum class TransitTimeScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A transit time score as the transit time team keeps it. */
data class TransitTimeScore(
    var code: String,
    var title: String,
    var channel: String,
    var ratio: Double,
    var capacity: Int,
    var attributes: MutableMap<String, String>,
    var status: TransitTimeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeScoreStatus): Boolean {
        when (status) {
            TransitTimeScoreStatus.OPEN -> {
                when (next) {
                    TransitTimeScoreStatus.RESOLVED, TransitTimeScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            TransitTimeScoreStatus.IN_REVIEW -> {
                when (next) {
                    TransitTimeScoreStatus.RESOLVED -> return true
                }
                return false
            }
            TransitTimeScoreStatus.RESOLVED -> return next == TransitTimeScoreStatus.ARCHIVED
            TransitTimeScoreStatus.REOPENED -> return next == TransitTimeScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TransitTimeScoreStatus) {
        if (!canMoveTo(next)) {
            error("transit time score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round(ratio * capacity.toDouble() * 100) / 100

    fun snapshot(): TransitTimeScore {
        return TransitTimeScore(
            code = code,
            title = title,
            channel = channel,
            ratio = ratio,
            capacity = capacity,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a transit time score in status open. */
fun newTransitTimeScore(code: String, title: String): TransitTimeScore {
    return TransitTimeScore(
        code = code,
        title = title,
        channel = "",
        ratio = 0.0,
        capacity = 0,
        attributes = mutableMapOf<String, String>(),
        status = TransitTimeScoreStatus.OPEN,
    )
}
