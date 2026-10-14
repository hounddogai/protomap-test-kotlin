package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire agreement is in its life cycle. */
enum class TireAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tire agreement. Its code is unique within a tenant. */
data class TireAgreement(
    var code: String,
    var title: String,
    var startsAt: Int,
    var archived: Boolean,
    var source: String,
    var distanceKm: Double,
    var driverId: String,
    var expiresAt: Int,
    var vehicleId: String,
    var segment: String,
    var status: TireAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireAgreementStatus): Boolean {
        when (status) {
            TireAgreementStatus.OPEN -> return next == TireAgreementStatus.REOPENED
            TireAgreementStatus.IN_REVIEW -> {
                when (next) {
                    TireAgreementStatus.RESOLVED -> return true
                }
                return false
            }
            TireAgreementStatus.RESOLVED -> {
                when (next) {
                    TireAgreementStatus.REOPENED -> return true
                }
                return false
            }
            TireAgreementStatus.REOPENED -> return next == TireAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TireAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tire agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tire agreement"} $code"

    fun weightedExposure(): Double = round((expiresAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): TireAgreement {
        return TireAgreement(
            code = code,
            title = title,
            startsAt = startsAt,
            archived = archived,
            source = source,
            distanceKm = distanceKm,
            driverId = driverId,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tire agreement in status open. */
fun newTireAgreement(code: String, title: String): TireAgreement {
    return TireAgreement(
        code = code,
        title = title,
        startsAt = 0,
        archived = false,
        source = "",
        distanceKm = 0.0,
        driverId = "",
        expiresAt = 0,
        vehicleId = "",
        segment = "",
        status = TireAgreementStatus.OPEN,
    )
}
