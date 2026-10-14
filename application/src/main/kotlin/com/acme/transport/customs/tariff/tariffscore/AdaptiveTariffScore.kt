package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tariff score records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a adaptive tariff score is in its life cycle. */
enum class AdaptiveTariffScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive tariff score. Its code is unique within a tenant. */
data class AdaptiveTariffScore(
    var code: String,
    var label: String,
    var externalRef: String,
    var shiftCode: String,
    var ceiling: Double,
    var currency: String,
    var active: Boolean,
    var graceDays: Int,
    var status: AdaptiveTariffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTariffScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTariffScoreStatus): Boolean {
        when (status) {
            AdaptiveTariffScoreStatus.REQUESTED -> return next == AdaptiveTariffScoreStatus.GRANTED
            AdaptiveTariffScoreStatus.GRANTED -> return next == AdaptiveTariffScoreStatus.REVOKED || next == AdaptiveTariffScoreStatus.EXPIRED
            AdaptiveTariffScoreStatus.REVOKED -> {
                when (next) {
                    AdaptiveTariffScoreStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTariffScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tariff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive tariff score"} $code"

    fun netVolume(): Double = round(ceiling * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveTariffScore {
        return AdaptiveTariffScore(
            code = code,
            label = label,
            externalRef = externalRef,
            shiftCode = shiftCode,
            ceiling = ceiling,
            currency = currency,
            active = active,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive tariff score in status requested. */
fun newAdaptiveTariffScore(code: String, label: String): AdaptiveTariffScore {
    return AdaptiveTariffScore(
        code = code,
        label = label,
        externalRef = "",
        shiftCode = "",
        ceiling = 0.0,
        currency = "",
        active = false,
        graceDays = 0,
        status = AdaptiveTariffScoreStatus.REQUESTED,
    )
}
