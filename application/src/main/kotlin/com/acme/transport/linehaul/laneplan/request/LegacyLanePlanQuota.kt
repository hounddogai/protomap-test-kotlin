package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lane plan quota is in its life cycle. */
enum class LegacyLanePlanQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy lane plan quota, with what the planners need to act on it. */
data class LegacyLanePlanQuota(
    var code: String,
    var title: String,
    var phone: String,
    var odometer: Int,
    var segment: String,
    var depot: String,
    var sequence: Int,
    var updatedAt: Int,
    var country: String,
    var driverId: String,
    var discount: Double,
    var status: LegacyLanePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLanePlanQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLanePlanQuotaStatus): Boolean {
        when (status) {
            LegacyLanePlanQuotaStatus.REQUESTED -> {
                when (next) {
                    LegacyLanePlanQuotaStatus.GRANTED, LegacyLanePlanQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyLanePlanQuotaStatus.GRANTED -> {
                when (next) {
                    LegacyLanePlanQuotaStatus.REVOKED -> return true
                }
                return false
            }
            LegacyLanePlanQuotaStatus.REVOKED -> {
                when (next) {
                    LegacyLanePlanQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLanePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("legacy lane plan quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round(odometer.toDouble() * discount * 100) / 100

    fun snapshot(): LegacyLanePlanQuota {
        return LegacyLanePlanQuota(
            code = code,
            title = title,
            phone = phone,
            odometer = odometer,
            segment = segment,
            depot = depot,
            sequence = sequence,
            updatedAt = updatedAt,
            country = country,
            driverId = driverId,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy lane plan quota in status requested. */
fun newLegacyLanePlanQuota(code: String, title: String): LegacyLanePlanQuota {
    return LegacyLanePlanQuota(
        code = code,
        title = title,
        phone = "",
        odometer = 0,
        segment = "",
        depot = "",
        sequence = 0,
        updatedAt = 0,
        country = "",
        driverId = "",
        discount = 0.0,
        status = LegacyLanePlanQuotaStatus.REQUESTED,
    )
}
