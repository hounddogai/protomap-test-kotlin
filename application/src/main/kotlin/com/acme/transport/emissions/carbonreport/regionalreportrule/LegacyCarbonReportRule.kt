package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy carbon report rule records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a legacy carbon report rule is in its life cycle. */
enum class LegacyCarbonReportRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy carbon report rule as the carbon report team keeps it. */
data class LegacyCarbonReportRule(
    var code: String,
    var title: String,
    var limit: Int,
    var shiftCode: String,
    var channel: String,
    var etaMinutes: Int,
    var externalRef: String,
    var weight: Double,
    var source: String,
    var status: LegacyCarbonReportRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCarbonReportRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCarbonReportRuleStatus): Boolean {
        when (status) {
            LegacyCarbonReportRuleStatus.CREATED -> {
                when (next) {
                    LegacyCarbonReportRuleStatus.RETIRED -> return true
                }
                return false
            }
            LegacyCarbonReportRuleStatus.VALIDATED -> return next == LegacyCarbonReportRuleStatus.PUBLISHED || next == LegacyCarbonReportRuleStatus.RETIRED
            LegacyCarbonReportRuleStatus.PUBLISHED -> return next == LegacyCarbonReportRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyCarbonReportRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy carbon report rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((limit.toDouble() - weight) * 100) / 100

    fun snapshot(): LegacyCarbonReportRule {
        return LegacyCarbonReportRule(
            code = code,
            title = title,
            limit = limit,
            shiftCode = shiftCode,
            channel = channel,
            etaMinutes = etaMinutes,
            externalRef = externalRef,
            weight = weight,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy carbon report rule in status created. */
fun newLegacyCarbonReportRule(code: String, title: String): LegacyCarbonReportRule {
    return LegacyCarbonReportRule(
        code = code,
        title = title,
        limit = 0,
        shiftCode = "",
        channel = "",
        etaMinutes = 0,
        externalRef = "",
        weight = 0.0,
        source = "",
        status = LegacyCarbonReportRuleStatus.CREATED,
    )
}
