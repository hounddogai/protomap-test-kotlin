package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal ev charge quota records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a seasonal ev charge quota is in its life cycle. */
enum class SeasonalEvChargeQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal ev charge quota as the ev charge team keeps it. */
data class SeasonalEvChargeQuota(
    var code: String,
    var label: String,
    var attempts: Int,
    var approved: Boolean,
    var name: String,
    var shiftCode: String,
    var summary: String,
    var active: Boolean,
    var visible: Boolean,
    var etaMinutes: Int,
    var startsAt: Int,
    var status: SeasonalEvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEvChargeQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEvChargeQuotaStatus): Boolean {
        when (status) {
            SeasonalEvChargeQuotaStatus.NEW -> return next == SeasonalEvChargeQuotaStatus.RUNNING || next == SeasonalEvChargeQuotaStatus.DONE
            SeasonalEvChargeQuotaStatus.QUEUED -> return next == SeasonalEvChargeQuotaStatus.DONE
            SeasonalEvChargeQuotaStatus.RUNNING -> {
                when (next) {
                    SeasonalEvChargeQuotaStatus.DONE -> return true
                }
                return false
            }
            SeasonalEvChargeQuotaStatus.DONE -> {
                when (next) {
                    SeasonalEvChargeQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalEvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal ev charge quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(startsAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): SeasonalEvChargeQuota {
        return SeasonalEvChargeQuota(
            code = code,
            label = label,
            attempts = attempts,
            approved = approved,
            name = name,
            shiftCode = shiftCode,
            summary = summary,
            active = active,
            visible = visible,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal ev charge quota in status new. */
fun newSeasonalEvChargeQuota(code: String, label: String): SeasonalEvChargeQuota {
    return SeasonalEvChargeQuota(
        code = code,
        label = label,
        attempts = 0,
        approved = false,
        name = "",
        shiftCode = "",
        summary = "",
        active = false,
        visible = false,
        etaMinutes = 0,
        startsAt = 0,
        status = SeasonalEvChargeQuotaStatus.NEW,
    )
}
