package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global carbon report policy is in its life cycle. */
enum class GlobalCarbonReportPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global carbon report policy as the carbon report team keeps it. */
data class GlobalCarbonReportPolicy(
    var code: String,
    var name: String,
    var margin: Double,
    var reviewer: String,
    var currency: String,
    var phone: String,
    var startsAt: Int,
    var endsAt: Int,
    var active: Boolean,
    var priority: Int,
    var status: GlobalCarbonReportPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCarbonReportPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCarbonReportPolicyStatus): Boolean {
        when (status) {
            GlobalCarbonReportPolicyStatus.PROPOSED -> return next == GlobalCarbonReportPolicyStatus.REJECTED || next == GlobalCarbonReportPolicyStatus.WITHDRAWN
            GlobalCarbonReportPolicyStatus.APPROVED -> {
                when (next) {
                    GlobalCarbonReportPolicyStatus.REJECTED -> return true
                }
                return false
            }
            GlobalCarbonReportPolicyStatus.REJECTED -> {
                when (next) {
                    GlobalCarbonReportPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCarbonReportPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global carbon report policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global carbon report policy"} $code"

    fun projectedExposure(): Double = round((startsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalCarbonReportPolicy {
        return GlobalCarbonReportPolicy(
            code = code,
            name = name,
            margin = margin,
            reviewer = reviewer,
            currency = currency,
            phone = phone,
            startsAt = startsAt,
            endsAt = endsAt,
            active = active,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global carbon report policy in status proposed. */
fun newGlobalCarbonReportPolicy(code: String, name: String): GlobalCarbonReportPolicy {
    return GlobalCarbonReportPolicy(
        code = code,
        name = name,
        margin = 0.0,
        reviewer = "",
        currency = "",
        phone = "",
        startsAt = 0,
        endsAt = 0,
        active = false,
        priority = 0,
        status = GlobalCarbonReportPolicyStatus.PROPOSED,
    )
}
