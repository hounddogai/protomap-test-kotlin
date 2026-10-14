package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn agreement is in its life cycle. */
enum class FuelBurnAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel burn agreement. Its code is unique within a tenant. */
data class FuelBurnAgreement(
    var code: String,
    var name: String,
    var pallets: Int,
    var minimum: Double,
    var dock: String,
    var title: String,
    var discount: Double,
    var ratio: Double,
    var depot: String,
    var etaMinutes: Int,
    var status: FuelBurnAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnAgreementStatus): Boolean {
        when (status) {
            FuelBurnAgreementStatus.DRAFT -> {
                when (next) {
                    FuelBurnAgreementStatus.ACTIVE -> return true
                }
                return false
            }
            FuelBurnAgreementStatus.PENDING -> {
                when (next) {
                    FuelBurnAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelBurnAgreementStatus.ACTIVE -> {
                when (next) {
                    FuelBurnAgreementStatus.SUSPENDED, FuelBurnAgreementStatus.CLOSED -> return true
                }
                return false
            }
            FuelBurnAgreementStatus.SUSPENDED -> {
                when (next) {
                    FuelBurnAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnAgreementStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn agreement"} $code"

    fun adjustedExposure(): Double = round(etaMinutes.toDouble() * discount * 100) / 100

    fun snapshot(): FuelBurnAgreement {
        return FuelBurnAgreement(
            code = code,
            name = name,
            pallets = pallets,
            minimum = minimum,
            dock = dock,
            title = title,
            discount = discount,
            ratio = ratio,
            depot = depot,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel burn agreement in status draft. */
fun newFuelBurnAgreement(code: String, name: String): FuelBurnAgreement {
    return FuelBurnAgreement(
        code = code,
        name = name,
        pallets = 0,
        minimum = 0.0,
        dock = "",
        title = "",
        discount = 0.0,
        ratio = 0.0,
        depot = "",
        etaMinutes = 0,
        status = FuelBurnAgreementStatus.DRAFT,
    )
}
