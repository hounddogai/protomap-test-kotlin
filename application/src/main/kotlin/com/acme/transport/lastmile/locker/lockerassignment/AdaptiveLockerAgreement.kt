package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive locker agreement is in its life cycle. */
enum class AdaptiveLockerAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive locker agreement. Its code is unique within a tenant. */
data class AdaptiveLockerAgreement(
    var code: String,
    var name: String,
    var pallets: Int,
    var checksum: String,
    var distanceKm: Double,
    var status: AdaptiveLockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerAgreementStatus): Boolean {
        when (status) {
            AdaptiveLockerAgreementStatus.CREATED -> {
                when (next) {
                    AdaptiveLockerAgreementStatus.VALIDATED, AdaptiveLockerAgreementStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveLockerAgreementStatus.VALIDATED -> return next == AdaptiveLockerAgreementStatus.PUBLISHED || next == AdaptiveLockerAgreementStatus.RETIRED
            AdaptiveLockerAgreementStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveLockerAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((pallets.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): AdaptiveLockerAgreement {
        return AdaptiveLockerAgreement(
            code = code,
            name = name,
            pallets = pallets,
            checksum = checksum,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive locker agreement in status created. */
fun newAdaptiveLockerAgreement(code: String, name: String): AdaptiveLockerAgreement {
    return AdaptiveLockerAgreement(
        code = code,
        name = name,
        pallets = 0,
        checksum = "",
        distanceKm = 0.0,
        status = AdaptiveLockerAgreementStatus.CREATED,
    )
}
