package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn quota is in its life cycle. */
enum class RollingFuelBurnQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling fuel burn quota and its current status. */
data class RollingFuelBurnQuota(
    var code: String,
    var title: String,
    var discount: Double,
    var updatedAt: Int,
    var notes: String,
    var label: String,
    var weight: Double,
    var approved: Boolean,
    var status: RollingFuelBurnQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnQuotaStatus): Boolean {
        when (status) {
            RollingFuelBurnQuotaStatus.NEW -> {
                when (next) {
                    RollingFuelBurnQuotaStatus.QUEUED -> return true
                }
                return false
            }
            RollingFuelBurnQuotaStatus.QUEUED -> {
                when (next) {
                    RollingFuelBurnQuotaStatus.RUNNING -> return true
                }
                return false
            }
            RollingFuelBurnQuotaStatus.RUNNING -> return next == RollingFuelBurnQuotaStatus.DONE || next == RollingFuelBurnQuotaStatus.FAILED
            RollingFuelBurnQuotaStatus.DONE -> return next == RollingFuelBurnQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling fuel burn quota"} $code"

    fun adjustedValue(): Double = round((discount + weight) * 100) / 100

    fun snapshot(): RollingFuelBurnQuota {
        return RollingFuelBurnQuota(
            code = code,
            title = title,
            discount = discount,
            updatedAt = updatedAt,
            notes = notes,
            label = label,
            weight = weight,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling fuel burn quota in status new. */
fun newRollingFuelBurnQuota(code: String, title: String): RollingFuelBurnQuota {
    return RollingFuelBurnQuota(
        code = code,
        title = title,
        discount = 0.0,
        updatedAt = 0,
        notes = "",
        label = "",
        weight = 0.0,
        approved = false,
        status = RollingFuelBurnQuotaStatus.NEW,
    )
}
