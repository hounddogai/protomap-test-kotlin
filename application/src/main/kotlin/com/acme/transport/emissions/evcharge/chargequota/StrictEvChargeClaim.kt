package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.text.normalize

/** Where a strict ev charge claim is in its life cycle. */
enum class StrictEvChargeClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict ev charge claim. Its code is unique within a tenant. */
data class StrictEvChargeClaim(
    var code: String,
    var name: String,
    var depot: String,
    var summary: String,
    var routeCode: String,
    var category: String,
    var active: Boolean,
    var amount: Double,
    var status: StrictEvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeClaimStatus): Boolean {
        when (status) {
            StrictEvChargeClaimStatus.PLANNED -> {
                when (next) {
                    StrictEvChargeClaimStatus.SCHEDULED, StrictEvChargeClaimStatus.CANCELLED -> return true
                }
                return false
            }
            StrictEvChargeClaimStatus.SCHEDULED -> {
                when (next) {
                    StrictEvChargeClaimStatus.IN_PROGRESS, StrictEvChargeClaimStatus.COMPLETED -> return true
                }
                return false
            }
            StrictEvChargeClaimStatus.IN_PROGRESS -> return next == StrictEvChargeClaimStatus.COMPLETED || next == StrictEvChargeClaimStatus.CANCELLED
            StrictEvChargeClaimStatus.COMPLETED -> return next == StrictEvChargeClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictEvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict ev charge claim"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): StrictEvChargeClaim {
        return StrictEvChargeClaim(
            code = code,
            name = name,
            depot = depot,
            summary = summary,
            routeCode = routeCode,
            category = category,
            active = active,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict ev charge claim in status planned. */
fun newStrictEvChargeClaim(code: String, name: String): StrictEvChargeClaim {
    return StrictEvChargeClaim(
        code = code,
        name = name,
        depot = "",
        summary = "",
        routeCode = "",
        category = "",
        active = false,
        amount = 0.0,
        status = StrictEvChargeClaimStatus.PLANNED,
    )
}
