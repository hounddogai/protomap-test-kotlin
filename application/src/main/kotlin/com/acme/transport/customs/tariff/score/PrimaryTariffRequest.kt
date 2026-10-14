package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tariff request is in its life cycle. */
enum class PrimaryTariffRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary tariff request as the tariff team keeps it. */
data class PrimaryTariffRequest(
    var code: String,
    var name: String,
    var weight: Double,
    var priority: Int,
    var locale: String,
    var reviewer: String,
    var description: String,
    var distanceKm: Double,
    var startsAt: Int,
    var status: PrimaryTariffRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTariffRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTariffRequestStatus): Boolean {
        when (status) {
            PrimaryTariffRequestStatus.PROPOSED -> {
                when (next) {
                    PrimaryTariffRequestStatus.REJECTED, PrimaryTariffRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTariffRequestStatus.APPROVED -> {
                when (next) {
                    PrimaryTariffRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTariffRequestStatus.REJECTED -> {
                when (next) {
                    PrimaryTariffRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTariffRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary tariff request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((startsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): PrimaryTariffRequest {
        return PrimaryTariffRequest(
            code = code,
            name = name,
            weight = weight,
            priority = priority,
            locale = locale,
            reviewer = reviewer,
            description = description,
            distanceKm = distanceKm,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary tariff request in status proposed. */
fun newPrimaryTariffRequest(code: String, name: String): PrimaryTariffRequest {
    return PrimaryTariffRequest(
        code = code,
        name = name,
        weight = 0.0,
        priority = 0,
        locale = "",
        reviewer = "",
        description = "",
        distanceKm = 0.0,
        startsAt = 0,
        status = PrimaryTariffRequestStatus.PROPOSED,
    )
}
