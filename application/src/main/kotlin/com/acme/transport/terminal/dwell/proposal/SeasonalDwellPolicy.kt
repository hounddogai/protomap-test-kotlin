package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dwell policy is in its life cycle. */
enum class SeasonalDwellPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal dwell policy as the dwell team keeps it. */
data class SeasonalDwellPolicy(
    var code: String,
    var title: String,
    var source: String,
    var ratio: Double,
    var durationMinutes: Int,
    var externalRef: String,
    var maximum: Double,
    var channel: String,
    var status: SeasonalDwellPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDwellPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDwellPolicyStatus): Boolean {
        when (status) {
            SeasonalDwellPolicyStatus.DRAFT -> return next == SeasonalDwellPolicyStatus.ACTIVE || next == SeasonalDwellPolicyStatus.SUSPENDED
            SeasonalDwellPolicyStatus.PENDING -> {
                when (next) {
                    SeasonalDwellPolicyStatus.ACTIVE -> return true
                }
                return false
            }
            SeasonalDwellPolicyStatus.ACTIVE -> return next == SeasonalDwellPolicyStatus.SUSPENDED
            SeasonalDwellPolicyStatus.SUSPENDED -> return next == SeasonalDwellPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalDwellPolicyStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dwell policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(maximum * ratio * 100) / 100

    fun snapshot(): SeasonalDwellPolicy {
        return SeasonalDwellPolicy(
            code = code,
            title = title,
            source = source,
            ratio = ratio,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            maximum = maximum,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal dwell policy in status draft. */
fun newSeasonalDwellPolicy(code: String, title: String): SeasonalDwellPolicy {
    return SeasonalDwellPolicy(
        code = code,
        title = title,
        source = "",
        ratio = 0.0,
        durationMinutes = 0,
        externalRef = "",
        maximum = 0.0,
        channel = "",
        status = SeasonalDwellPolicyStatus.DRAFT,
    )
}
