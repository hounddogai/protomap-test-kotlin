package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire bundle is in its life cycle. */
enum class TireBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tire bundle as the tire team keeps it. */
data class TireBundle(
    var code: String,
    var name: String,
    var source: String,
    var routeCode: String,
    var durationMinutes: Int,
    var region: String,
    var odometer: Int,
    var status: TireBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireBundleStatus): Boolean {
        when (status) {
            TireBundleStatus.REQUESTED -> {
                when (next) {
                    TireBundleStatus.EXPIRED -> return true
                }
                return false
            }
            TireBundleStatus.GRANTED -> return next == TireBundleStatus.REVOKED || next == TireBundleStatus.EXPIRED
            TireBundleStatus.REVOKED -> {
                when (next) {
                    TireBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireBundleStatus) {
        if (!canMoveTo(next)) {
            error("tire bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((odometer.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TireBundle {
        return TireBundle(
            code = code,
            name = name,
            source = source,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            region = region,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tire bundle in status requested. */
fun newTireBundle(code: String, name: String): TireBundle {
    return TireBundle(
        code = code,
        name = name,
        source = "",
        routeCode = "",
        durationMinutes = 0,
        region = "",
        odometer = 0,
        status = TireBundleStatus.REQUESTED,
    )
}
