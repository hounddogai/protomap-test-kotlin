package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional relay baseline is in its life cycle. */
enum class RegionalRelayBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional relay baseline, with what the planners need to act on it. */
data class RegionalRelayBaseline(
    var code: String,
    var label: String,
    var version: Int,
    var reviewer: String,
    var ceiling: Double,
    var ratio: Double,
    var batchSize: Int,
    var timezone: String,
    var status: RegionalRelayBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRelayBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRelayBaselineStatus): Boolean {
        when (status) {
            RegionalRelayBaselineStatus.DRAFT -> return next == RegionalRelayBaselineStatus.PENDING || next == RegionalRelayBaselineStatus.CLOSED
            RegionalRelayBaselineStatus.PENDING -> {
                when (next) {
                    RegionalRelayBaselineStatus.SUSPENDED, RegionalRelayBaselineStatus.CLOSED -> return true
                }
                return false
            }
            RegionalRelayBaselineStatus.ACTIVE -> {
                when (next) {
                    RegionalRelayBaselineStatus.SUSPENDED, RegionalRelayBaselineStatus.CLOSED -> return true
                }
                return false
            }
            RegionalRelayBaselineStatus.SUSPENDED -> return next == RegionalRelayBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalRelayBaselineStatus) {
        if (!canMoveTo(next)) {
            error("regional relay baseline $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((version.toDouble() + ceiling) * 100) / 100

    fun snapshot(): RegionalRelayBaseline {
        return RegionalRelayBaseline(
            code = code,
            label = label,
            version = version,
            reviewer = reviewer,
            ceiling = ceiling,
            ratio = ratio,
            batchSize = batchSize,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional relay baseline in status draft. */
fun newRegionalRelayBaseline(code: String, label: String): RegionalRelayBaseline {
    return RegionalRelayBaseline(
        code = code,
        label = label,
        version = 0,
        reviewer = "",
        ceiling = 0.0,
        ratio = 0.0,
        batchSize = 0,
        timezone = "",
        status = RegionalRelayBaselineStatus.DRAFT,
    )
}
