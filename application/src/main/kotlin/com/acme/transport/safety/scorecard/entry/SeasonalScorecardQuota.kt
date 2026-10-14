package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize

/** Where a seasonal scorecard quota is in its life cycle. */
enum class SeasonalScorecardQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal scorecard quota and its current status. */
data class SeasonalScorecardQuota(
    var code: String,
    var name: String,
    var locale: String,
    var country: String,
    var endsAt: Int,
    var routeCode: String,
    var channel: String,
    var status: SeasonalScorecardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalScorecardQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalScorecardQuotaStatus): Boolean {
        when (status) {
            SeasonalScorecardQuotaStatus.DRAFT -> {
                when (next) {
                    SeasonalScorecardQuotaStatus.PENDING -> return true
                }
                return false
            }
            SeasonalScorecardQuotaStatus.PENDING -> return next == SeasonalScorecardQuotaStatus.SUSPENDED
            SeasonalScorecardQuotaStatus.ACTIVE -> return next == SeasonalScorecardQuotaStatus.SUSPENDED || next == SeasonalScorecardQuotaStatus.CLOSED
            SeasonalScorecardQuotaStatus.SUSPENDED -> return next == SeasonalScorecardQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalScorecardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal scorecard quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): SeasonalScorecardQuota {
        return SeasonalScorecardQuota(
            code = code,
            name = name,
            locale = locale,
            country = country,
            endsAt = endsAt,
            routeCode = routeCode,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal scorecard quota in status draft. */
fun newSeasonalScorecardQuota(code: String, name: String): SeasonalScorecardQuota {
    return SeasonalScorecardQuota(
        code = code,
        name = name,
        locale = "",
        country = "",
        endsAt = 0,
        routeCode = "",
        channel = "",
        status = SeasonalScorecardQuotaStatus.DRAFT,
    )
}
