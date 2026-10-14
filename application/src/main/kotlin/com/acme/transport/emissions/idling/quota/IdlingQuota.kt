package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling quota records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling quota is in its life cycle. */
enum class IdlingQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A idling quota as the idling team keeps it. */
data class IdlingQuota(
    var code: String,
    var label: String,
    var vehicleId: String,
    var summary: String,
    var region: String,
    var batchSize: Int,
    var country: String,
    var score: Double,
    var routeCode: String,
    var unit: String,
    var createdAt: Int,
    var status: IdlingQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingQuotaStatus): Boolean {
        when (status) {
            IdlingQuotaStatus.DRAFT -> {
                when (next) {
                    IdlingQuotaStatus.ACTIVE, IdlingQuotaStatus.CLOSED -> return true
                }
                return false
            }
            IdlingQuotaStatus.PENDING -> return next == IdlingQuotaStatus.ACTIVE || next == IdlingQuotaStatus.CLOSED
            IdlingQuotaStatus.ACTIVE -> {
                when (next) {
                    IdlingQuotaStatus.SUSPENDED, IdlingQuotaStatus.CLOSED -> return true
                }
                return false
            }
            IdlingQuotaStatus.SUSPENDED -> return next == IdlingQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IdlingQuotaStatus) {
        if (!canMoveTo(next)) {
            error("idling quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"idling quota"} $code"

    fun adjustedLoad(): Double = round((createdAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): IdlingQuota {
        return IdlingQuota(
            code = code,
            label = label,
            vehicleId = vehicleId,
            summary = summary,
            region = region,
            batchSize = batchSize,
            country = country,
            score = score,
            routeCode = routeCode,
            unit = unit,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a idling quota in status draft. */
fun newIdlingQuota(code: String, label: String): IdlingQuota {
    return IdlingQuota(
        code = code,
        label = label,
        vehicleId = "",
        summary = "",
        region = "",
        batchSize = 0,
        country = "",
        score = 0.0,
        routeCode = "",
        unit = "",
        createdAt = 0,
        status = IdlingQuotaStatus.DRAFT,
    )
}
