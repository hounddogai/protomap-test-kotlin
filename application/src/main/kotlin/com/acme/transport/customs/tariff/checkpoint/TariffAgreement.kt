package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff agreement is in its life cycle. */
enum class TariffAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tariff agreement, with what the planners need to act on it. */
data class TariffAgreement(
    var code: String,
    var label: String,
    var discount: Double,
    var pinned: Boolean,
    var maximum: Double,
    var carrier: String,
    var approved: Boolean,
    var cost: Double,
    var lane: String,
    var odometer: Int,
    var status: TariffAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffAgreementStatus): Boolean {
        when (status) {
            TariffAgreementStatus.NEW -> return next == TariffAgreementStatus.RUNNING
            TariffAgreementStatus.QUEUED -> {
                when (next) {
                    TariffAgreementStatus.RUNNING -> return true
                }
                return false
            }
            TariffAgreementStatus.RUNNING -> return next == TariffAgreementStatus.DONE
            TariffAgreementStatus.DONE -> {
                when (next) {
                    TariffAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tariff agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedTotal(): Double = round((discount - odometer.toDouble()) * 100) / 100

    fun snapshot(): TariffAgreement {
        return TariffAgreement(
            code = code,
            label = label,
            discount = discount,
            pinned = pinned,
            maximum = maximum,
            carrier = carrier,
            approved = approved,
            cost = cost,
            lane = lane,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tariff agreement in status new. */
fun newTariffAgreement(code: String, label: String): TariffAgreement {
    return TariffAgreement(
        code = code,
        label = label,
        discount = 0.0,
        pinned = false,
        maximum = 0.0,
        carrier = "",
        approved = false,
        cost = 0.0,
        lane = "",
        odometer = 0,
        status = TariffAgreementStatus.NEW,
    )
}
