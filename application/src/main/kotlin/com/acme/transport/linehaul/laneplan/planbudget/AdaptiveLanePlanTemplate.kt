package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan template is in its life cycle. */
enum class AdaptiveLanePlanTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive lane plan template. Its code is unique within a tenant. */
data class AdaptiveLanePlanTemplate(
    var code: String,
    var name: String,
    var batchSize: Int,
    var threshold: Double,
    var externalRef: String,
    var active: Boolean,
    var shiftCode: String,
    var ceiling: Double,
    var timezone: String,
    var carrier: String,
    var status: AdaptiveLanePlanTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanTemplateStatus): Boolean {
        when (status) {
            AdaptiveLanePlanTemplateStatus.REQUESTED -> {
                when (next) {
                    AdaptiveLanePlanTemplateStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveLanePlanTemplateStatus.GRANTED -> return next == AdaptiveLanePlanTemplateStatus.REVOKED || next == AdaptiveLanePlanTemplateStatus.EXPIRED
            AdaptiveLanePlanTemplateStatus.REVOKED -> {
                when (next) {
                    AdaptiveLanePlanTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((ceiling - threshold) * 100) / 100

    fun snapshot(): AdaptiveLanePlanTemplate {
        return AdaptiveLanePlanTemplate(
            code = code,
            name = name,
            batchSize = batchSize,
            threshold = threshold,
            externalRef = externalRef,
            active = active,
            shiftCode = shiftCode,
            ceiling = ceiling,
            timezone = timezone,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive lane plan template in status requested. */
fun newAdaptiveLanePlanTemplate(code: String, name: String): AdaptiveLanePlanTemplate {
    return AdaptiveLanePlanTemplate(
        code = code,
        name = name,
        batchSize = 0,
        threshold = 0.0,
        externalRef = "",
        active = false,
        shiftCode = "",
        ceiling = 0.0,
        timezone = "",
        carrier = "",
        status = AdaptiveLanePlanTemplateStatus.REQUESTED,
    )
}
