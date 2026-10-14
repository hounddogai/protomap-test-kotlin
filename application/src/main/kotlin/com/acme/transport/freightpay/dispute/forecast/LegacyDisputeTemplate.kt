package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dispute template is in its life cycle. */
enum class LegacyDisputeTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy dispute template and its current status. */
data class LegacyDisputeTemplate(
    var code: String,
    var label: String,
    var endsAt: Int,
    var cost: Double,
    var ratio: Double,
    var ceiling: Double,
    var priority: Int,
    var limit: Int,
    var vehicleId: String,
    var category: String,
    var driverId: String,
    var status: LegacyDisputeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDisputeTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDisputeTemplateStatus): Boolean {
        when (status) {
            LegacyDisputeTemplateStatus.PROPOSED -> return next == LegacyDisputeTemplateStatus.REJECTED
            LegacyDisputeTemplateStatus.APPROVED -> {
                when (next) {
                    LegacyDisputeTemplateStatus.REJECTED -> return true
                }
                return false
            }
            LegacyDisputeTemplateStatus.REJECTED -> {
                when (next) {
                    LegacyDisputeTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDisputeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("legacy dispute template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((ratio + limit.toDouble()) * 100) / 100

    fun snapshot(): LegacyDisputeTemplate {
        return LegacyDisputeTemplate(
            code = code,
            label = label,
            endsAt = endsAt,
            cost = cost,
            ratio = ratio,
            ceiling = ceiling,
            priority = priority,
            limit = limit,
            vehicleId = vehicleId,
            category = category,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy dispute template in status proposed. */
fun newLegacyDisputeTemplate(code: String, label: String): LegacyDisputeTemplate {
    return LegacyDisputeTemplate(
        code = code,
        label = label,
        endsAt = 0,
        cost = 0.0,
        ratio = 0.0,
        ceiling = 0.0,
        priority = 0,
        limit = 0,
        vehicleId = "",
        category = "",
        driverId = "",
        status = LegacyDisputeTemplateStatus.PROPOSED,
    )
}
