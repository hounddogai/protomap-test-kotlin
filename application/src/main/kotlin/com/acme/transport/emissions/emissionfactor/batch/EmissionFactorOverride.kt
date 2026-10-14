package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize

/** Where a emission factor override is in its life cycle. */
enum class EmissionFactorOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One emission factor override, with what the planners need to act on it. */
data class EmissionFactorOverride(
    var code: String,
    var name: String,
    var checksum: String,
    var locale: String,
    var routeCode: String,
    var segment: String,
    var dock: String,
    var country: String,
    var quantity: Int,
    var status: EmissionFactorOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorOverrideStatus): Boolean {
        when (status) {
            EmissionFactorOverrideStatus.PLANNED -> return next == EmissionFactorOverrideStatus.COMPLETED
            EmissionFactorOverrideStatus.SCHEDULED -> return next == EmissionFactorOverrideStatus.COMPLETED
            EmissionFactorOverrideStatus.IN_PROGRESS -> return next == EmissionFactorOverrideStatus.COMPLETED || next == EmissionFactorOverrideStatus.CANCELLED
            EmissionFactorOverrideStatus.COMPLETED -> {
                when (next) {
                    EmissionFactorOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorOverrideStatus) {
        if (!canMoveTo(next)) {
            error("emission factor override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor override"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): EmissionFactorOverride {
        return EmissionFactorOverride(
            code = code,
            name = name,
            checksum = checksum,
            locale = locale,
            routeCode = routeCode,
            segment = segment,
            dock = dock,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a emission factor override in status planned. */
fun newEmissionFactorOverride(code: String, name: String): EmissionFactorOverride {
    return EmissionFactorOverride(
        code = code,
        name = name,
        checksum = "",
        locale = "",
        routeCode = "",
        segment = "",
        dock = "",
        country = "",
        quantity = 0,
        status = EmissionFactorOverrideStatus.PLANNED,
    )
}
