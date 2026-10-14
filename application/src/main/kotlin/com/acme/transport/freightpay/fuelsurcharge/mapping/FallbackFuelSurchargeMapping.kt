package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize

// The fallback fuel surcharge mapping records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fallback fuel surcharge mapping is in its life cycle. */
enum class FallbackFuelSurchargeMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback fuel surcharge mapping as the fuel surcharge team keeps it. */
data class FallbackFuelSurchargeMapping(
    var code: String,
    var name: String,
    var capacity: Int,
    var approved: Boolean,
    var description: String,
    var status: FallbackFuelSurchargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelSurchargeMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelSurchargeMappingStatus): Boolean {
        when (status) {
            FallbackFuelSurchargeMappingStatus.IDLE -> {
                when (next) {
                    FallbackFuelSurchargeMappingStatus.STOPPED -> return true
                }
                return false
            }
            FallbackFuelSurchargeMappingStatus.WARMING -> return next == FallbackFuelSurchargeMappingStatus.READY
            FallbackFuelSurchargeMappingStatus.READY -> return next == FallbackFuelSurchargeMappingStatus.DRAINING
            FallbackFuelSurchargeMappingStatus.DRAINING -> {
                when (next) {
                    FallbackFuelSurchargeMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFuelSurchargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel surcharge mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): FallbackFuelSurchargeMapping {
        return FallbackFuelSurchargeMapping(
            code = code,
            name = name,
            capacity = capacity,
            approved = approved,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback fuel surcharge mapping in status idle. */
fun newFallbackFuelSurchargeMapping(code: String, name: String): FallbackFuelSurchargeMapping {
    return FallbackFuelSurchargeMapping(
        code = code,
        name = name,
        capacity = 0,
        approved = false,
        description = "",
        status = FallbackFuelSurchargeMappingStatus.IDLE,
    )
}
