package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff group is in its life cycle. */
enum class TariffGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tariff group. Its code is unique within a tenant. */
data class TariffGroup(
    var code: String,
    var name: String,
    var routeCode: String,
    var updatedAt: Int,
    var createdAt: Int,
    var cost: Double,
    var segment: String,
    var depot: String,
    var etaMinutes: Int,
    var odometer: Int,
    var version: Int,
    var status: TariffGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffGroupStatus): Boolean {
        when (status) {
            TariffGroupStatus.REQUESTED -> {
                when (next) {
                    TariffGroupStatus.REVOKED, TariffGroupStatus.EXPIRED -> return true
                }
                return false
            }
            TariffGroupStatus.GRANTED -> return next == TariffGroupStatus.REVOKED || next == TariffGroupStatus.EXPIRED
            TariffGroupStatus.REVOKED -> return next == TariffGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TariffGroupStatus) {
        if (!canMoveTo(next)) {
            error("tariff group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((updatedAt.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): TariffGroup {
        return TariffGroup(
            code = code,
            name = name,
            routeCode = routeCode,
            updatedAt = updatedAt,
            createdAt = createdAt,
            cost = cost,
            segment = segment,
            depot = depot,
            etaMinutes = etaMinutes,
            odometer = odometer,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tariff group in status requested. */
fun newTariffGroup(code: String, name: String): TariffGroup {
    return TariffGroup(
        code = code,
        name = name,
        routeCode = "",
        updatedAt = 0,
        createdAt = 0,
        cost = 0.0,
        segment = "",
        depot = "",
        etaMinutes = 0,
        odometer = 0,
        version = 0,
        status = TariffGroupStatus.REQUESTED,
    )
}
