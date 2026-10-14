package com.acme.transport.customs.hold.baseline

import com.acme.transport.kit.text.normalize

// The hold baseline records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold baseline is in its life cycle. */
enum class HoldBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One hold baseline, with what the planners need to act on it. */
data class HoldBaseline(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var source: String,
    var channel: String,
    var dock: String,
    var country: String,
    var status: HoldBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldBaselineStatus): Boolean {
        when (status) {
            HoldBaselineStatus.REQUESTED -> {
                when (next) {
                    HoldBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            HoldBaselineStatus.GRANTED -> {
                when (next) {
                    HoldBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            HoldBaselineStatus.REVOKED -> return next == HoldBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HoldBaselineStatus) {
        if (!canMoveTo(next)) {
            error("hold baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hold baseline"} $code"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): HoldBaseline {
        return HoldBaseline(
            code = code,
            title = title,
            distanceKm = distanceKm,
            source = source,
            channel = channel,
            dock = dock,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hold baseline in status requested. */
fun newHoldBaseline(code: String, title: String): HoldBaseline {
    return HoldBaseline(
        code = code,
        title = title,
        distanceKm = 0.0,
        source = "",
        channel = "",
        dock = "",
        country = "",
        status = HoldBaselineStatus.REQUESTED,
    )
}
