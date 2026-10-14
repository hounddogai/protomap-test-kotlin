package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling emission factor quota is in its life cycle. */
enum class RollingEmissionFactorQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling emission factor quota. Its code is unique within a tenant. */
data class RollingEmissionFactorQuota(
    var code: String,
    var name: String,
    var summary: String,
    var attempts: Int,
    var locale: String,
    var active: Boolean,
    var windowDays: Int,
    var status: RollingEmissionFactorQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingEmissionFactorQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingEmissionFactorQuotaStatus): Boolean {
        when (status) {
            RollingEmissionFactorQuotaStatus.NEW -> {
                when (next) {
                    RollingEmissionFactorQuotaStatus.RUNNING, RollingEmissionFactorQuotaStatus.FAILED -> return true
                }
                return false
            }
            RollingEmissionFactorQuotaStatus.QUEUED -> {
                when (next) {
                    RollingEmissionFactorQuotaStatus.FAILED -> return true
                }
                return false
            }
            RollingEmissionFactorQuotaStatus.RUNNING -> return next == RollingEmissionFactorQuotaStatus.DONE || next == RollingEmissionFactorQuotaStatus.FAILED
            RollingEmissionFactorQuotaStatus.DONE -> {
                when (next) {
                    RollingEmissionFactorQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingEmissionFactorQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling emission factor quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling emission factor quota"} $code"

    fun netTotal(): Double = round(attempts.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): RollingEmissionFactorQuota {
        return RollingEmissionFactorQuota(
            code = code,
            name = name,
            summary = summary,
            attempts = attempts,
            locale = locale,
            active = active,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling emission factor quota in status new. */
fun newRollingEmissionFactorQuota(code: String, name: String): RollingEmissionFactorQuota {
    return RollingEmissionFactorQuota(
        code = code,
        name = name,
        summary = "",
        attempts = 0,
        locale = "",
        active = false,
        windowDays = 0,
        status = RollingEmissionFactorQuotaStatus.NEW,
    )
}
