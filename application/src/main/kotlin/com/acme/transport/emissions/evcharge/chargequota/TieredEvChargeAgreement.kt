package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge agreement is in its life cycle. */
enum class TieredEvChargeAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered ev charge agreement, with what the planners need to act on it. */
data class TieredEvChargeAgreement(
    var code: String,
    var name: String,
    var ratio: Double,
    var rate: Double,
    var currency: String,
    var margin: Double,
    var shiftCode: String,
    var retries: Int,
    var status: TieredEvChargeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeAgreementStatus): Boolean {
        when (status) {
            TieredEvChargeAgreementStatus.IDLE -> {
                when (next) {
                    TieredEvChargeAgreementStatus.DRAINING -> return true
                }
                return false
            }
            TieredEvChargeAgreementStatus.WARMING -> {
                when (next) {
                    TieredEvChargeAgreementStatus.READY -> return true
                }
                return false
            }
            TieredEvChargeAgreementStatus.READY -> {
                when (next) {
                    TieredEvChargeAgreementStatus.DRAINING, TieredEvChargeAgreementStatus.STOPPED -> return true
                }
                return false
            }
            TieredEvChargeAgreementStatus.DRAINING -> {
                when (next) {
                    TieredEvChargeAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredEvChargeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((margin + retries.toDouble()) * 100) / 100

    fun snapshot(): TieredEvChargeAgreement {
        return TieredEvChargeAgreement(
            code = code,
            name = name,
            ratio = ratio,
            rate = rate,
            currency = currency,
            margin = margin,
            shiftCode = shiftCode,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered ev charge agreement in status idle. */
fun newTieredEvChargeAgreement(code: String, name: String): TieredEvChargeAgreement {
    return TieredEvChargeAgreement(
        code = code,
        name = name,
        ratio = 0.0,
        rate = 0.0,
        currency = "",
        margin = 0.0,
        shiftCode = "",
        retries = 0,
        status = TieredEvChargeAgreementStatus.IDLE,
    )
}
