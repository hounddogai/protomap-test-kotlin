package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break claim is in its life cycle. */
enum class RestBreakClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rest break claim, with what the planners need to act on it. */
data class RestBreakClaim(
    var code: String,
    var label: String,
    var timezone: String,
    var limit: Int,
    var channel: String,
    var driverId: String,
    var version: Int,
    var windowDays: Int,
    var weight: Double,
    var rate: Double,
    var status: RestBreakClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakClaimStatus): Boolean {
        when (status) {
            RestBreakClaimStatus.NEW -> return next == RestBreakClaimStatus.RUNNING || next == RestBreakClaimStatus.FAILED
            RestBreakClaimStatus.QUEUED -> return next == RestBreakClaimStatus.DONE
            RestBreakClaimStatus.RUNNING -> {
                when (next) {
                    RestBreakClaimStatus.DONE, RestBreakClaimStatus.FAILED -> return true
                }
                return false
            }
            RestBreakClaimStatus.DONE -> return next == RestBreakClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RestBreakClaimStatus) {
        if (!canMoveTo(next)) {
            error("rest break claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((rate + limit.toDouble()) * 100) / 100

    fun snapshot(): RestBreakClaim {
        return RestBreakClaim(
            code = code,
            label = label,
            timezone = timezone,
            limit = limit,
            channel = channel,
            driverId = driverId,
            version = version,
            windowDays = windowDays,
            weight = weight,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break claim in status new. */
fun newRestBreakClaim(code: String, label: String): RestBreakClaim {
    return RestBreakClaim(
        code = code,
        label = label,
        timezone = "",
        limit = 0,
        channel = "",
        driverId = "",
        version = 0,
        windowDays = 0,
        weight = 0.0,
        rate = 0.0,
        status = RestBreakClaimStatus.NEW,
    )
}
