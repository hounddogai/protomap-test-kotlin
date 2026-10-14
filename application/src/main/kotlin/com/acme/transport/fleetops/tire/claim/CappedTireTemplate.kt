package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tire template is in its life cycle. */
enum class CappedTireTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped tire template and its current status. */
data class CappedTireTemplate(
    var code: String,
    var label: String,
    var region: String,
    var budget: Double,
    var startsAt: Int,
    var currency: String,
    var limit: Int,
    var status: CappedTireTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTireTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTireTemplateStatus): Boolean {
        when (status) {
            CappedTireTemplateStatus.PLANNED -> {
                when (next) {
                    CappedTireTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            CappedTireTemplateStatus.SCHEDULED -> return next == CappedTireTemplateStatus.CANCELLED
            CappedTireTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    CappedTireTemplateStatus.COMPLETED, CappedTireTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            CappedTireTemplateStatus.COMPLETED -> return next == CappedTireTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedTireTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped tire template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round(budget * limit.toDouble() * 100) / 100

    fun snapshot(): CappedTireTemplate {
        return CappedTireTemplate(
            code = code,
            label = label,
            region = region,
            budget = budget,
            startsAt = startsAt,
            currency = currency,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped tire template in status planned. */
fun newCappedTireTemplate(code: String, label: String): CappedTireTemplate {
    return CappedTireTemplate(
        code = code,
        label = label,
        region = "",
        budget = 0.0,
        startsAt = 0,
        currency = "",
        limit = 0,
        status = CappedTireTemplateStatus.PLANNED,
    )
}
