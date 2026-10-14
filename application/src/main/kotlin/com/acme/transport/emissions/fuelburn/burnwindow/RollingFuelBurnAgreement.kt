package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn agreement is in its life cycle. */
enum class RollingFuelBurnAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling fuel burn agreement as the fuel burn team keeps it. */
data class RollingFuelBurnAgreement(
    var code: String,
    var label: String,
    var ceiling: Double,
    var weight: Double,
    var odometer: Int,
    var discount: Double,
    var tags: MutableList<String>,
    var quantity: Int,
    var status: RollingFuelBurnAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnAgreementStatus): Boolean {
        when (status) {
            RollingFuelBurnAgreementStatus.PLANNED -> {
                when (next) {
                    RollingFuelBurnAgreementStatus.SCHEDULED, RollingFuelBurnAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            RollingFuelBurnAgreementStatus.SCHEDULED -> {
                when (next) {
                    RollingFuelBurnAgreementStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingFuelBurnAgreementStatus.IN_PROGRESS -> return next == RollingFuelBurnAgreementStatus.COMPLETED || next == RollingFuelBurnAgreementStatus.CANCELLED
            RollingFuelBurnAgreementStatus.COMPLETED -> {
                when (next) {
                    RollingFuelBurnAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((ceiling - odometer.toDouble()) * 100) / 100

    fun snapshot(): RollingFuelBurnAgreement {
        return RollingFuelBurnAgreement(
            code = code,
            label = label,
            ceiling = ceiling,
            weight = weight,
            odometer = odometer,
            discount = discount,
            tags = tags,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling fuel burn agreement in status planned. */
fun newRollingFuelBurnAgreement(code: String, label: String): RollingFuelBurnAgreement {
    return RollingFuelBurnAgreement(
        code = code,
        label = label,
        ceiling = 0.0,
        weight = 0.0,
        odometer = 0,
        discount = 0.0,
        tags = mutableListOf<String>(),
        quantity = 0,
        status = RollingFuelBurnAgreementStatus.PLANNED,
    )
}
