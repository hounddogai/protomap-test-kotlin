package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive emission factor mapping records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a adaptive emission factor mapping is in its life cycle. */
enum class AdaptiveEmissionFactorMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive emission factor mapping. Its code is unique within a tenant. */
data class AdaptiveEmissionFactorMapping(
    var code: String,
    var name: String,
    var createdAt: Int,
    var carrier: String,
    var phone: String,
    var routeCode: String,
    var odometer: Int,
    var region: String,
    var durationMinutes: Int,
    var status: AdaptiveEmissionFactorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorMappingStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorMappingStatus.CREATED -> {
                when (next) {
                    AdaptiveEmissionFactorMappingStatus.PUBLISHED, AdaptiveEmissionFactorMappingStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorMappingStatus.VALIDATED -> {
                when (next) {
                    AdaptiveEmissionFactorMappingStatus.PUBLISHED, AdaptiveEmissionFactorMappingStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorMappingStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveEmissionFactorMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorMappingStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive emission factor mapping"} $code"

    fun effectiveValue(): Double = round(odometer.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorMapping {
        return AdaptiveEmissionFactorMapping(
            code = code,
            name = name,
            createdAt = createdAt,
            carrier = carrier,
            phone = phone,
            routeCode = routeCode,
            odometer = odometer,
            region = region,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive emission factor mapping in status created. */
fun newAdaptiveEmissionFactorMapping(code: String, name: String): AdaptiveEmissionFactorMapping {
    return AdaptiveEmissionFactorMapping(
        code = code,
        name = name,
        createdAt = 0,
        carrier = "",
        phone = "",
        routeCode = "",
        odometer = 0,
        region = "",
        durationMinutes = 0,
        status = AdaptiveEmissionFactorMappingStatus.CREATED,
    )
}
