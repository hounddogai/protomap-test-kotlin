package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor quota is in its life cycle. */
enum class EmissionFactorQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A emission factor quota. Its code is unique within a tenant. */
data class EmissionFactorQuota(
    var code: String,
    var label: String,
    var lane: String,
    var capacity: Int,
    var channel: String,
    var routeCode: String,
    var discount: Double,
    var status: EmissionFactorQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorQuotaStatus): Boolean {
        when (status) {
            EmissionFactorQuotaStatus.DRAFT -> {
                when (next) {
                    EmissionFactorQuotaStatus.ACTIVE -> return true
                }
                return false
            }
            EmissionFactorQuotaStatus.PENDING -> {
                when (next) {
                    EmissionFactorQuotaStatus.ACTIVE, EmissionFactorQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            EmissionFactorQuotaStatus.ACTIVE -> {
                when (next) {
                    EmissionFactorQuotaStatus.SUSPENDED, EmissionFactorQuotaStatus.CLOSED -> return true
                }
                return false
            }
            EmissionFactorQuotaStatus.SUSPENDED -> return next == EmissionFactorQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: EmissionFactorQuotaStatus) {
        if (!canMoveTo(next)) {
            error("emission factor quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"emission factor quota"} $code"

    fun adjustedLoad(): Double = round((discount + capacity.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorQuota {
        return EmissionFactorQuota(
            code = code,
            label = label,
            lane = lane,
            capacity = capacity,
            channel = channel,
            routeCode = routeCode,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a emission factor quota in status draft. */
fun newEmissionFactorQuota(code: String, label: String): EmissionFactorQuota {
    return EmissionFactorQuota(
        code = code,
        label = label,
        lane = "",
        capacity = 0,
        channel = "",
        routeCode = "",
        discount = 0.0,
        status = EmissionFactorQuotaStatus.DRAFT,
    )
}
