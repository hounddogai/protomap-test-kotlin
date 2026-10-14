package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.text.normalize

// The fuel surcharge policy records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge policy is in its life cycle. */
enum class FuelSurchargePolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fuel surcharge policy as the fuel surcharge team keeps it. */
data class FuelSurchargePolicy(
    var code: String,
    var title: String,
    var notes: String,
    var source: String,
    var stopCount: Int,
    var archived: Boolean,
    var carrier: String,
    var status: FuelSurchargePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargePolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargePolicyStatus): Boolean {
        when (status) {
            FuelSurchargePolicyStatus.PROPOSED -> {
                when (next) {
                    FuelSurchargePolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            FuelSurchargePolicyStatus.APPROVED -> return next == FuelSurchargePolicyStatus.WITHDRAWN
            FuelSurchargePolicyStatus.REJECTED -> return next == FuelSurchargePolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FuelSurchargePolicyStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge policy"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): FuelSurchargePolicy {
        return FuelSurchargePolicy(
            code = code,
            title = title,
            notes = notes,
            source = source,
            stopCount = stopCount,
            archived = archived,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fuel surcharge policy in status proposed. */
fun newFuelSurchargePolicy(code: String, title: String): FuelSurchargePolicy {
    return FuelSurchargePolicy(
        code = code,
        title = title,
        notes = "",
        source = "",
        stopCount = 0,
        archived = false,
        carrier = "",
        status = FuelSurchargePolicyStatus.PROPOSED,
    )
}
