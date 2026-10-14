package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize

// The tiered carbon report quota records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a tiered carbon report quota is in its life cycle. */
enum class TieredCarbonReportQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered carbon report quota as the carbon report team keeps it. */
data class TieredCarbonReportQuota(
    var code: String,
    var label: String,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var amount: Double,
    var status: TieredCarbonReportQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCarbonReportQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCarbonReportQuotaStatus): Boolean {
        when (status) {
            TieredCarbonReportQuotaStatus.IDLE -> {
                when (next) {
                    TieredCarbonReportQuotaStatus.READY, TieredCarbonReportQuotaStatus.STOPPED -> return true
                }
                return false
            }
            TieredCarbonReportQuotaStatus.WARMING -> {
                when (next) {
                    TieredCarbonReportQuotaStatus.READY -> return true
                }
                return false
            }
            TieredCarbonReportQuotaStatus.READY -> return next == TieredCarbonReportQuotaStatus.STOPPED
            TieredCarbonReportQuotaStatus.DRAINING -> {
                when (next) {
                    TieredCarbonReportQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCarbonReportQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tiered carbon report quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): TieredCarbonReportQuota {
        return TieredCarbonReportQuota(
            code = code,
            label = label,
            contactEmail = contactEmail,
            attributes = attributes,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered carbon report quota in status idle. */
fun newTieredCarbonReportQuota(code: String, label: String): TieredCarbonReportQuota {
    return TieredCarbonReportQuota(
        code = code,
        label = label,
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        amount = 0.0,
        status = TieredCarbonReportQuotaStatus.IDLE,
    )
}
