package com.acme.transport.freightpay.ratecard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rate card template is in its life cycle. */
enum class FallbackRateCardTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One fallback rate card template, with what the planners need to act on it. */
data class FallbackRateCardTemplate(
    var code: String,
    var label: String,
    var discount: Double,
    var trailerId: String,
    var externalRef: String,
    var maximum: Double,
    var retries: Int,
    var status: FallbackRateCardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRateCardTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRateCardTemplateStatus): Boolean {
        when (status) {
            FallbackRateCardTemplateStatus.CREATED -> {
                when (next) {
                    FallbackRateCardTemplateStatus.PUBLISHED, FallbackRateCardTemplateStatus.RETIRED -> return true
                }
                return false
            }
            FallbackRateCardTemplateStatus.VALIDATED -> return next == FallbackRateCardTemplateStatus.PUBLISHED
            FallbackRateCardTemplateStatus.PUBLISHED -> return next == FallbackRateCardTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackRateCardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("fallback rate card template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback rate card template"} $code"

    fun effectiveLoad(): Double = round((maximum + retries.toDouble()) * 100) / 100

    fun snapshot(): FallbackRateCardTemplate {
        return FallbackRateCardTemplate(
            code = code,
            label = label,
            discount = discount,
            trailerId = trailerId,
            externalRef = externalRef,
            maximum = maximum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback rate card template in status created. */
fun newFallbackRateCardTemplate(code: String, label: String): FallbackRateCardTemplate {
    return FallbackRateCardTemplate(
        code = code,
        label = label,
        discount = 0.0,
        trailerId = "",
        externalRef = "",
        maximum = 0.0,
        retries = 0,
        status = FallbackRateCardTemplateStatus.CREATED,
    )
}
