package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card rule is in its life cycle. */
enum class LenientRateCardRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient rate card rule. Its code is unique within a tenant. */
data class LenientRateCardRule(
    var code: String,
    var label: String,
    var minimum: Double,
    var shiftCode: String,
    var distanceKm: Double,
    var endsAt: Int,
    var status: LenientRateCardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardRuleStatus): Boolean {
        when (status) {
            LenientRateCardRuleStatus.CREATED -> return next == LenientRateCardRuleStatus.RETIRED
            LenientRateCardRuleStatus.VALIDATED -> {
                when (next) {
                    LenientRateCardRuleStatus.PUBLISHED, LenientRateCardRuleStatus.RETIRED -> return true
                }
                return false
            }
            LenientRateCardRuleStatus.PUBLISHED -> return next == LenientRateCardRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRateCardRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedTotal(): Double = round((minimum - distanceKm) * 100) / 100

    fun snapshot(): LenientRateCardRule {
        return LenientRateCardRule(
            code = code,
            label = label,
            minimum = minimum,
            shiftCode = shiftCode,
            distanceKm = distanceKm,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient rate card rule in status created. */
fun newLenientRateCardRule(code: String, label: String): LenientRateCardRule {
    return LenientRateCardRule(
        code = code,
        label = label,
        minimum = 0.0,
        shiftCode = "",
        distanceKm = 0.0,
        endsAt = 0,
        status = LenientRateCardRuleStatus.CREATED,
    )
}
