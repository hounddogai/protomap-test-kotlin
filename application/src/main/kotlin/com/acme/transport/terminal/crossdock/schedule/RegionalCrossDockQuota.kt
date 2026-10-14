package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cross dock quota is in its life cycle. */
enum class RegionalCrossDockQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One regional cross dock quota, with what the planners need to act on it. */
data class RegionalCrossDockQuota(
    var code: String,
    var label: String,
    var contactEmail: String,
    var windowDays: Int,
    var floorValue: Double,
    var status: RegionalCrossDockQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCrossDockQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCrossDockQuotaStatus): Boolean {
        when (status) {
            RegionalCrossDockQuotaStatus.PROPOSED -> {
                when (next) {
                    RegionalCrossDockQuotaStatus.APPROVED -> return true
                }
                return false
            }
            RegionalCrossDockQuotaStatus.APPROVED -> {
                when (next) {
                    RegionalCrossDockQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCrossDockQuotaStatus.REJECTED -> return next == RegionalCrossDockQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalCrossDockQuotaStatus) {
        if (!canMoveTo(next)) {
            error("regional cross dock quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((windowDays.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RegionalCrossDockQuota {
        return RegionalCrossDockQuota(
            code = code,
            label = label,
            contactEmail = contactEmail,
            windowDays = windowDays,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional cross dock quota in status proposed. */
fun newRegionalCrossDockQuota(code: String, label: String): RegionalCrossDockQuota {
    return RegionalCrossDockQuota(
        code = code,
        label = label,
        contactEmail = "",
        windowDays = 0,
        floorValue = 0.0,
        status = RegionalCrossDockQuotaStatus.PROPOSED,
    )
}
