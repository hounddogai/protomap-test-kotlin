package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive defect template records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a adaptive defect template is in its life cycle. */
enum class AdaptiveDefectTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a adaptive defect template and its current status. */
data class AdaptiveDefectTemplate(
    var code: String,
    var title: String,
    var attempts: Int,
    var summary: String,
    var floorValue: Double,
    var unit: String,
    var lane: String,
    var quantity: Int,
    var phone: String,
    var amount: Double,
    var priority: Int,
    var status: AdaptiveDefectTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDefectTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDefectTemplateStatus): Boolean {
        when (status) {
            AdaptiveDefectTemplateStatus.CREATED -> return next == AdaptiveDefectTemplateStatus.PUBLISHED || next == AdaptiveDefectTemplateStatus.RETIRED
            AdaptiveDefectTemplateStatus.VALIDATED -> return next == AdaptiveDefectTemplateStatus.PUBLISHED || next == AdaptiveDefectTemplateStatus.RETIRED
            AdaptiveDefectTemplateStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveDefectTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDefectTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive defect template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive defect template"} $code"

    fun projectedExposure(): Double = round((attempts.toDouble() - floorValue) * 100) / 100

    fun snapshot(): AdaptiveDefectTemplate {
        return AdaptiveDefectTemplate(
            code = code,
            title = title,
            attempts = attempts,
            summary = summary,
            floorValue = floorValue,
            unit = unit,
            lane = lane,
            quantity = quantity,
            phone = phone,
            amount = amount,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive defect template in status created. */
fun newAdaptiveDefectTemplate(code: String, title: String): AdaptiveDefectTemplate {
    return AdaptiveDefectTemplate(
        code = code,
        title = title,
        attempts = 0,
        summary = "",
        floorValue = 0.0,
        unit = "",
        lane = "",
        quantity = 0,
        phone = "",
        amount = 0.0,
        priority = 0,
        status = AdaptiveDefectTemplateStatus.CREATED,
    )
}
