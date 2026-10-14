package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel burn audit is in its life cycle. */
enum class RegionalFuelBurnAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional fuel burn audit as the fuel burn team keeps it. */
data class RegionalFuelBurnAudit(
    var code: String,
    var label: String,
    var externalRef: String,
    var timezone: String,
    var retries: Int,
    var discount: Double,
    var visible: Boolean,
    var amount: Double,
    var status: RegionalFuelBurnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnAuditStatus): Boolean {
        when (status) {
            RegionalFuelBurnAuditStatus.PROPOSED -> return next == RegionalFuelBurnAuditStatus.REJECTED
            RegionalFuelBurnAuditStatus.APPROVED -> {
                when (next) {
                    RegionalFuelBurnAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalFuelBurnAuditStatus.REJECTED -> {
                when (next) {
                    RegionalFuelBurnAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((discount - retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelBurnAudit {
        return RegionalFuelBurnAudit(
            code = code,
            label = label,
            externalRef = externalRef,
            timezone = timezone,
            retries = retries,
            discount = discount,
            visible = visible,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional fuel burn audit in status proposed. */
fun newRegionalFuelBurnAudit(code: String, label: String): RegionalFuelBurnAudit {
    return RegionalFuelBurnAudit(
        code = code,
        label = label,
        externalRef = "",
        timezone = "",
        retries = 0,
        discount = 0.0,
        visible = false,
        amount = 0.0,
        status = RegionalFuelBurnAuditStatus.PROPOSED,
    )
}
