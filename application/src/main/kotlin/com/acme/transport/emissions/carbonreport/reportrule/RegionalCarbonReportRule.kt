package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize

// The regional carbon report rule records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a regional carbon report rule is in its life cycle. */
enum class RegionalCarbonReportRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional carbon report rule as the carbon report team keeps it. */
data class RegionalCarbonReportRule(
    var code: String,
    var label: String,
    var contactEmail: String,
    var unit: String,
    var reviewer: String,
    var timezone: String,
    var active: Boolean,
    var revision: Int,
    var driverId: String,
    var region: String,
    var trailerId: String,
    var status: RegionalCarbonReportRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportRuleStatus): Boolean {
        when (status) {
            RegionalCarbonReportRuleStatus.DRAFT -> {
                when (next) {
                    RegionalCarbonReportRuleStatus.SUSPENDED, RegionalCarbonReportRuleStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCarbonReportRuleStatus.PENDING -> return next == RegionalCarbonReportRuleStatus.CLOSED
            RegionalCarbonReportRuleStatus.ACTIVE -> {
                when (next) {
                    RegionalCarbonReportRuleStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCarbonReportRuleStatus.SUSPENDED -> {
                when (next) {
                    RegionalCarbonReportRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"regional carbon report rule"} $code"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): RegionalCarbonReportRule {
        return RegionalCarbonReportRule(
            code = code,
            label = label,
            contactEmail = contactEmail,
            unit = unit,
            reviewer = reviewer,
            timezone = timezone,
            active = active,
            revision = revision,
            driverId = driverId,
            region = region,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional carbon report rule in status draft. */
fun newRegionalCarbonReportRule(code: String, label: String): RegionalCarbonReportRule {
    return RegionalCarbonReportRule(
        code = code,
        label = label,
        contactEmail = "",
        unit = "",
        reviewer = "",
        timezone = "",
        active = false,
        revision = 0,
        driverId = "",
        region = "",
        trailerId = "",
        status = RegionalCarbonReportRuleStatus.DRAFT,
    )
}
