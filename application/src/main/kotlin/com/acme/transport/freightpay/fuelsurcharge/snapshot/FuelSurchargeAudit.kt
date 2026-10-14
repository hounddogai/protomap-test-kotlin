package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge audit is in its life cycle. */
enum class FuelSurchargeAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fuel surcharge audit as the fuel surcharge team keeps it. */
data class FuelSurchargeAudit(
    var code: String,
    var title: String,
    var unit: String,
    var updatedAt: Int,
    var carrier: String,
    var status: FuelSurchargeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeAuditStatus): Boolean {
        when (status) {
            FuelSurchargeAuditStatus.IDLE -> return next == FuelSurchargeAuditStatus.WARMING || next == FuelSurchargeAuditStatus.STOPPED
            FuelSurchargeAuditStatus.WARMING -> {
                when (next) {
                    FuelSurchargeAuditStatus.READY -> return true
                }
                return false
            }
            FuelSurchargeAuditStatus.READY -> return next == FuelSurchargeAuditStatus.DRAINING || next == FuelSurchargeAuditStatus.STOPPED
            FuelSurchargeAuditStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeAuditStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): FuelSurchargeAudit {
        return FuelSurchargeAudit(
            code = code,
            title = title,
            unit = unit,
            updatedAt = updatedAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel surcharge audit in status idle. */
fun newFuelSurchargeAudit(code: String, title: String): FuelSurchargeAudit {
    return FuelSurchargeAudit(
        code = code,
        title = title,
        unit = "",
        updatedAt = 0,
        carrier = "",
        status = FuelSurchargeAuditStatus.IDLE,
    )
}
