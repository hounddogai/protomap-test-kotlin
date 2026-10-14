package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card template is in its life cycle. */
enum class LenientRateCardTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient rate card template and its current status. */
data class LenientRateCardTemplate(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var batchSize: Int,
    var ratio: Double,
    var source: String,
    var status: LenientRateCardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardTemplateStatus): Boolean {
        when (status) {
            LenientRateCardTemplateStatus.PLANNED -> return next == LenientRateCardTemplateStatus.IN_PROGRESS
            LenientRateCardTemplateStatus.SCHEDULED -> return next == LenientRateCardTemplateStatus.COMPLETED || next == LenientRateCardTemplateStatus.CANCELLED
            LenientRateCardTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    LenientRateCardTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            LenientRateCardTemplateStatus.COMPLETED -> return next == LenientRateCardTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientRateCardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient rate card template"} $code"

    fun projectedExposure(): Double = round(ratio * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): LenientRateCardTemplate {
        return LenientRateCardTemplate(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            batchSize = batchSize,
            ratio = ratio,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient rate card template in status planned. */
fun newLenientRateCardTemplate(code: String, label: String): LenientRateCardTemplate {
    return LenientRateCardTemplate(
        code = code,
        label = label,
        etaMinutes = 0,
        batchSize = 0,
        ratio = 0.0,
        source = "",
        status = LenientRateCardTemplateStatus.PLANNED,
    )
}
