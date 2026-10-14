package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal relay target records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a seasonal relay target is in its life cycle. */
enum class SeasonalRelayTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal relay target as the relay team keeps it. */
data class SeasonalRelayTarget(
    var code: String,
    var title: String,
    var threshold: Double,
    var quantity: Int,
    var dock: String,
    var version: Int,
    var status: SeasonalRelayTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRelayTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRelayTargetStatus): Boolean {
        when (status) {
            SeasonalRelayTargetStatus.REQUESTED -> return next == SeasonalRelayTargetStatus.EXPIRED
            SeasonalRelayTargetStatus.GRANTED -> {
                when (next) {
                    SeasonalRelayTargetStatus.REVOKED, SeasonalRelayTargetStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalRelayTargetStatus.REVOKED -> {
                when (next) {
                    SeasonalRelayTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRelayTargetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal relay target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((quantity.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRelayTarget {
        return SeasonalRelayTarget(
            code = code,
            title = title,
            threshold = threshold,
            quantity = quantity,
            dock = dock,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal relay target in status requested. */
fun newSeasonalRelayTarget(code: String, title: String): SeasonalRelayTarget {
    return SeasonalRelayTarget(
        code = code,
        title = title,
        threshold = 0.0,
        quantity = 0,
        dock = "",
        version = 0,
        status = SeasonalRelayTargetStatus.REQUESTED,
    )
}
