package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending rate card agreement records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a pending rate card agreement is in its life cycle. */
enum class PendingRateCardAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending rate card agreement as the rate card team keeps it. */
data class PendingRateCardAgreement(
    var code: String,
    var label: String,
    var vehicleId: String,
    var retries: Int,
    var dock: String,
    var etaMinutes: Int,
    var category: String,
    var timezone: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var pallets: Int,
    var status: PendingRateCardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRateCardAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRateCardAgreementStatus): Boolean {
        when (status) {
            PendingRateCardAgreementStatus.PLANNED -> {
                when (next) {
                    PendingRateCardAgreementStatus.IN_PROGRESS, PendingRateCardAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            PendingRateCardAgreementStatus.SCHEDULED -> return next == PendingRateCardAgreementStatus.CANCELLED
            PendingRateCardAgreementStatus.IN_PROGRESS -> return next == PendingRateCardAgreementStatus.CANCELLED
            PendingRateCardAgreementStatus.COMPLETED -> return next == PendingRateCardAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingRateCardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending rate card agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((pallets.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingRateCardAgreement {
        return PendingRateCardAgreement(
            code = code,
            label = label,
            vehicleId = vehicleId,
            retries = retries,
            dock = dock,
            etaMinutes = etaMinutes,
            category = category,
            timezone = timezone,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending rate card agreement in status planned. */
fun newPendingRateCardAgreement(code: String, label: String): PendingRateCardAgreement {
    return PendingRateCardAgreement(
        code = code,
        label = label,
        vehicleId = "",
        retries = 0,
        dock = "",
        etaMinutes = 0,
        category = "",
        timezone = "",
        expiresAt = 0,
        fuelLiters = 0.0,
        pallets = 0,
        status = PendingRateCardAgreementStatus.PLANNED,
    )
}
