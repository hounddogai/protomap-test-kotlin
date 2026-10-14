package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive near miss quota is in its life cycle. */
enum class AdaptiveNearMissQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive near miss quota as the near miss team keeps it. */
data class AdaptiveNearMissQuota(
    var code: String,
    var label: String,
    var notes: String,
    var rate: Double,
    var country: String,
    var expiresAt: Int,
    var weight: Double,
    var cost: Double,
    var archived: Boolean,
    var fuelLiters: Double,
    var distanceKm: Double,
    var status: AdaptiveNearMissQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissQuotaStatus): Boolean {
        when (status) {
            AdaptiveNearMissQuotaStatus.CREATED -> return next == AdaptiveNearMissQuotaStatus.VALIDATED || next == AdaptiveNearMissQuotaStatus.PUBLISHED
            AdaptiveNearMissQuotaStatus.VALIDATED -> return next == AdaptiveNearMissQuotaStatus.RETIRED
            AdaptiveNearMissQuotaStatus.PUBLISHED -> return next == AdaptiveNearMissQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissQuotaStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive near miss quota"} $code"

    fun projectedValue(): Double = round((distanceKm + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissQuota {
        return AdaptiveNearMissQuota(
            code = code,
            label = label,
            notes = notes,
            rate = rate,
            country = country,
            expiresAt = expiresAt,
            weight = weight,
            cost = cost,
            archived = archived,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive near miss quota in status created. */
fun newAdaptiveNearMissQuota(code: String, label: String): AdaptiveNearMissQuota {
    return AdaptiveNearMissQuota(
        code = code,
        label = label,
        notes = "",
        rate = 0.0,
        country = "",
        expiresAt = 0,
        weight = 0.0,
        cost = 0.0,
        archived = false,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        status = AdaptiveNearMissQuotaStatus.CREATED,
    )
}
