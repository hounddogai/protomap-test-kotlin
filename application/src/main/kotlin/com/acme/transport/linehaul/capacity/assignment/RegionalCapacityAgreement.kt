package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity agreement is in its life cycle. */
enum class RegionalCapacityAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional capacity agreement as the capacity team keeps it. */
data class RegionalCapacityAgreement(
    var code: String,
    var label: String,
    var quantity: Int,
    var fuelLiters: Double,
    var vehicleId: String,
    var status: RegionalCapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityAgreementStatus): Boolean {
        when (status) {
            RegionalCapacityAgreementStatus.DRAFT -> {
                when (next) {
                    RegionalCapacityAgreementStatus.PENDING, RegionalCapacityAgreementStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCapacityAgreementStatus.PENDING -> {
                when (next) {
                    RegionalCapacityAgreementStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCapacityAgreementStatus.ACTIVE -> return next == RegionalCapacityAgreementStatus.CLOSED
            RegionalCapacityAgreementStatus.SUSPENDED -> {
                when (next) {
                    RegionalCapacityAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((fuelLiters + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalCapacityAgreement {
        return RegionalCapacityAgreement(
            code = code,
            label = label,
            quantity = quantity,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional capacity agreement in status draft. */
fun newRegionalCapacityAgreement(code: String, label: String): RegionalCapacityAgreement {
    return RegionalCapacityAgreement(
        code = code,
        label = label,
        quantity = 0,
        fuelLiters = 0.0,
        vehicleId = "",
        status = RegionalCapacityAgreementStatus.DRAFT,
    )
}
