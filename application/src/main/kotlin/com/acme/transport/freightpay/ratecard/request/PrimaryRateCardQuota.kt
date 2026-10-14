package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary rate card quota is in its life cycle. */
enum class PrimaryRateCardQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary rate card quota. Its code is unique within a tenant. */
data class PrimaryRateCardQuota(
    var code: String,
    var title: String,
    var currency: String,
    var startsAt: Int,
    var segment: String,
    var dock: String,
    var stopCount: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var priority: Int,
    var floorValue: Double,
    var status: PrimaryRateCardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRateCardQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRateCardQuotaStatus): Boolean {
        when (status) {
            PrimaryRateCardQuotaStatus.REQUESTED -> {
                when (next) {
                    PrimaryRateCardQuotaStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryRateCardQuotaStatus.GRANTED -> {
                when (next) {
                    PrimaryRateCardQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryRateCardQuotaStatus.REVOKED -> return next == PrimaryRateCardQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryRateCardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary rate card quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary rate card quota"} $code"

    fun weightedValue(): Double = round(startsAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): PrimaryRateCardQuota {
        return PrimaryRateCardQuota(
            code = code,
            title = title,
            currency = currency,
            startsAt = startsAt,
            segment = segment,
            dock = dock,
            stopCount = stopCount,
            visible = visible,
            distanceKm = distanceKm,
            priority = priority,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary rate card quota in status requested. */
fun newPrimaryRateCardQuota(code: String, title: String): PrimaryRateCardQuota {
    return PrimaryRateCardQuota(
        code = code,
        title = title,
        currency = "",
        startsAt = 0,
        segment = "",
        dock = "",
        stopCount = 0,
        visible = false,
        distanceKm = 0.0,
        priority = 0,
        floorValue = 0.0,
        status = PrimaryRateCardQuotaStatus.REQUESTED,
    )
}
