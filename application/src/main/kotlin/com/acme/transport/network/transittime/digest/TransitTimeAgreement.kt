package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time agreement is in its life cycle. */
enum class TransitTimeAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A transit time agreement as the transit time team keeps it. */
data class TransitTimeAgreement(
    var code: String,
    var label: String,
    var pallets: Int,
    var fuelLiters: Double,
    var category: String,
    var description: String,
    var notes: String,
    var status: TransitTimeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeAgreementStatus): Boolean {
        when (status) {
            TransitTimeAgreementStatus.REQUESTED -> {
                when (next) {
                    TransitTimeAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeAgreementStatus.GRANTED -> return next == TransitTimeAgreementStatus.REVOKED
            TransitTimeAgreementStatus.REVOKED -> return next == TransitTimeAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("transit time agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((fuelLiters + pallets.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeAgreement {
        return TransitTimeAgreement(
            code = code,
            label = label,
            pallets = pallets,
            fuelLiters = fuelLiters,
            category = category,
            description = description,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time agreement in status requested. */
fun newTransitTimeAgreement(code: String, label: String): TransitTimeAgreement {
    return TransitTimeAgreement(
        code = code,
        label = label,
        pallets = 0,
        fuelLiters = 0.0,
        category = "",
        description = "",
        notes = "",
        status = TransitTimeAgreementStatus.REQUESTED,
    )
}
