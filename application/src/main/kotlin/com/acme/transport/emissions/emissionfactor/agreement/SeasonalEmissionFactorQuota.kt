package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.text.normalize

/** Where a seasonal emission factor quota is in its life cycle. */
enum class SeasonalEmissionFactorQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal emission factor quota and its current status. */
data class SeasonalEmissionFactorQuota(
    var code: String,
    var name: String,
    var timezone: String,
    var country: String,
    var source: String,
    var contactEmail: String,
    var locale: String,
    var stopCount: Int,
    var status: SeasonalEmissionFactorQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorQuotaStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorQuotaStatus.IDLE -> {
                when (next) {
                    SeasonalEmissionFactorQuotaStatus.READY -> return true
                }
                return false
            }
            SeasonalEmissionFactorQuotaStatus.WARMING -> return next == SeasonalEmissionFactorQuotaStatus.READY
            SeasonalEmissionFactorQuotaStatus.READY -> {
                when (next) {
                    SeasonalEmissionFactorQuotaStatus.DRAINING, SeasonalEmissionFactorQuotaStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalEmissionFactorQuotaStatus.DRAINING -> return next == SeasonalEmissionFactorQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal emission factor quota"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): SeasonalEmissionFactorQuota {
        return SeasonalEmissionFactorQuota(
            code = code,
            name = name,
            timezone = timezone,
            country = country,
            source = source,
            contactEmail = contactEmail,
            locale = locale,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal emission factor quota in status idle. */
fun newSeasonalEmissionFactorQuota(code: String, name: String): SeasonalEmissionFactorQuota {
    return SeasonalEmissionFactorQuota(
        code = code,
        name = name,
        timezone = "",
        country = "",
        source = "",
        contactEmail = "",
        locale = "",
        stopCount = 0,
        status = SeasonalEmissionFactorQuotaStatus.IDLE,
    )
}
