package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted route offset quota is in its life cycle. */
enum class WeightedRouteOffsetQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted route offset quota. Its code is unique within a tenant. */
data class WeightedRouteOffsetQuota(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var archived: Boolean,
    var driverId: String,
    var phone: String,
    var cost: Double,
    var routeCode: String,
    var status: WeightedRouteOffsetQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRouteOffsetQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRouteOffsetQuotaStatus): Boolean {
        when (status) {
            WeightedRouteOffsetQuotaStatus.DRAFT -> return next == WeightedRouteOffsetQuotaStatus.PENDING || next == WeightedRouteOffsetQuotaStatus.CLOSED
            WeightedRouteOffsetQuotaStatus.PENDING -> {
                when (next) {
                    WeightedRouteOffsetQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedRouteOffsetQuotaStatus.ACTIVE -> {
                when (next) {
                    WeightedRouteOffsetQuotaStatus.SUSPENDED, WeightedRouteOffsetQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedRouteOffsetQuotaStatus.SUSPENDED -> {
                when (next) {
                    WeightedRouteOffsetQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRouteOffsetQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted route offset quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((distanceKm + cost) * 100) / 100

    fun snapshot(): WeightedRouteOffsetQuota {
        return WeightedRouteOffsetQuota(
            code = code,
            label = label,
            distanceKm = distanceKm,
            archived = archived,
            driverId = driverId,
            phone = phone,
            cost = cost,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted route offset quota in status draft. */
fun newWeightedRouteOffsetQuota(code: String, label: String): WeightedRouteOffsetQuota {
    return WeightedRouteOffsetQuota(
        code = code,
        label = label,
        distanceKm = 0.0,
        archived = false,
        driverId = "",
        phone = "",
        cost = 0.0,
        routeCode = "",
        status = WeightedRouteOffsetQuotaStatus.DRAFT,
    )
}
