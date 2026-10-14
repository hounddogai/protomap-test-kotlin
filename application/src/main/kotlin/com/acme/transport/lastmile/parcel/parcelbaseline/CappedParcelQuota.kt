package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped parcel quota is in its life cycle. */
enum class CappedParcelQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped parcel quota as the parcel team keeps it. */
data class CappedParcelQuota(
    var code: String,
    var title: String,
    var timezone: String,
    var rate: Double,
    var priority: Int,
    var weight: Double,
    var unit: String,
    var durationMinutes: Int,
    var graceDays: Int,
    var label: String,
    var quantity: Int,
    var status: CappedParcelQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedParcelQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedParcelQuotaStatus): Boolean {
        when (status) {
            CappedParcelQuotaStatus.REQUESTED -> return next == CappedParcelQuotaStatus.GRANTED || next == CappedParcelQuotaStatus.EXPIRED
            CappedParcelQuotaStatus.GRANTED -> {
                when (next) {
                    CappedParcelQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            CappedParcelQuotaStatus.REVOKED -> {
                when (next) {
                    CappedParcelQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedParcelQuotaStatus) {
        if (!canMoveTo(next)) {
            error("capped parcel quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun weightedValue(): Double = round(weight * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): CappedParcelQuota {
        return CappedParcelQuota(
            code = code,
            title = title,
            timezone = timezone,
            rate = rate,
            priority = priority,
            weight = weight,
            unit = unit,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            label = label,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped parcel quota in status requested. */
fun newCappedParcelQuota(code: String, title: String): CappedParcelQuota {
    return CappedParcelQuota(
        code = code,
        title = title,
        timezone = "",
        rate = 0.0,
        priority = 0,
        weight = 0.0,
        unit = "",
        durationMinutes = 0,
        graceDays = 0,
        label = "",
        quantity = 0,
        status = CappedParcelQuotaStatus.REQUESTED,
    )
}
