package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual carbon report rule records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a manual carbon report rule is in its life cycle. */
enum class ManualCarbonReportRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual carbon report rule as the carbon report team keeps it. */
data class ManualCarbonReportRule(
    var code: String,
    var label: String,
    var locale: String,
    var maximum: Double,
    var pallets: Int,
    var capacity: Int,
    var phone: String,
    var discount: Double,
    var quantity: Int,
    var status: ManualCarbonReportRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCarbonReportRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCarbonReportRuleStatus): Boolean {
        when (status) {
            ManualCarbonReportRuleStatus.OPEN -> return next == ManualCarbonReportRuleStatus.REOPENED
            ManualCarbonReportRuleStatus.IN_REVIEW -> {
                when (next) {
                    ManualCarbonReportRuleStatus.RESOLVED, ManualCarbonReportRuleStatus.REOPENED -> return true
                }
                return false
            }
            ManualCarbonReportRuleStatus.RESOLVED -> {
                when (next) {
                    ManualCarbonReportRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualCarbonReportRuleStatus.REOPENED -> return next == ManualCarbonReportRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualCarbonReportRuleStatus) {
        if (!canMoveTo(next)) {
            error("manual carbon report rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual carbon report rule"} $code"

    fun effectiveVolume(): Double = round(pallets.toDouble() * maximum * 100) / 100

    fun snapshot(): ManualCarbonReportRule {
        return ManualCarbonReportRule(
            code = code,
            label = label,
            locale = locale,
            maximum = maximum,
            pallets = pallets,
            capacity = capacity,
            phone = phone,
            discount = discount,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual carbon report rule in status open. */
fun newManualCarbonReportRule(code: String, label: String): ManualCarbonReportRule {
    return ManualCarbonReportRule(
        code = code,
        label = label,
        locale = "",
        maximum = 0.0,
        pallets = 0,
        capacity = 0,
        phone = "",
        discount = 0.0,
        quantity = 0,
        status = ManualCarbonReportRuleStatus.OPEN,
    )
}
