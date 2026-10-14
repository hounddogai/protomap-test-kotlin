package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal relay baseline is in its life cycle. */
enum class SeasonalRelayBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal relay baseline as the relay team keeps it. */
data class SeasonalRelayBaseline(
    var code: String,
    var name: String,
    var dock: String,
    var graceDays: Int,
    var routeCode: String,
    var limit: Int,
    var approved: Boolean,
    var country: String,
    var revision: Int,
    var status: SeasonalRelayBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRelayBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRelayBaselineStatus): Boolean {
        when (status) {
            SeasonalRelayBaselineStatus.NEW -> return next == SeasonalRelayBaselineStatus.QUEUED || next == SeasonalRelayBaselineStatus.FAILED
            SeasonalRelayBaselineStatus.QUEUED -> return next == SeasonalRelayBaselineStatus.DONE
            SeasonalRelayBaselineStatus.RUNNING -> {
                when (next) {
                    SeasonalRelayBaselineStatus.FAILED -> return true
                }
                return false
            }
            SeasonalRelayBaselineStatus.DONE -> {
                when (next) {
                    SeasonalRelayBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRelayBaselineStatus) {
        if (!canMoveTo(next)) {
            error("seasonal relay baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"seasonal relay baseline"} $code"

    fun adjustedVolume(): Double = round(limit.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): SeasonalRelayBaseline {
        return SeasonalRelayBaseline(
            code = code,
            name = name,
            dock = dock,
            graceDays = graceDays,
            routeCode = routeCode,
            limit = limit,
            approved = approved,
            country = country,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal relay baseline in status new. */
fun newSeasonalRelayBaseline(code: String, name: String): SeasonalRelayBaseline {
    return SeasonalRelayBaseline(
        code = code,
        name = name,
        dock = "",
        graceDays = 0,
        routeCode = "",
        limit = 0,
        approved = false,
        country = "",
        revision = 0,
        status = SeasonalRelayBaselineStatus.NEW,
    )
}
