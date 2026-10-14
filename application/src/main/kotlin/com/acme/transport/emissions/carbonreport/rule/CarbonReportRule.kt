package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report rule records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report rule is in its life cycle. */
enum class CarbonReportRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report rule. Its code is unique within a tenant. */
data class CarbonReportRule(
    var code: String,
    var name: String,
    var country: String,
    var ratio: Double,
    var tags: MutableList<String>,
    var routeCode: String,
    var maximum: Double,
    var notes: String,
    var status: CarbonReportRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportRuleStatus): Boolean {
        when (status) {
            CarbonReportRuleStatus.NEW -> return next == CarbonReportRuleStatus.DONE
            CarbonReportRuleStatus.QUEUED -> return next == CarbonReportRuleStatus.FAILED
            CarbonReportRuleStatus.RUNNING -> return next == CarbonReportRuleStatus.DONE
            CarbonReportRuleStatus.DONE -> {
                when (next) {
                    CarbonReportRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportRuleStatus) {
        if (!canMoveTo(next)) {
            error("carbon report rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((maximum + ratio) * 100) / 100

    fun snapshot(): CarbonReportRule {
        return CarbonReportRule(
            code = code,
            name = name,
            country = country,
            ratio = ratio,
            tags = tags,
            routeCode = routeCode,
            maximum = maximum,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a carbon report rule in status new. */
fun newCarbonReportRule(code: String, name: String): CarbonReportRule {
    return CarbonReportRule(
        code = code,
        name = name,
        country = "",
        ratio = 0.0,
        tags = mutableListOf<String>(),
        routeCode = "",
        maximum = 0.0,
        notes = "",
        status = CarbonReportRuleStatus.NEW,
    )
}
