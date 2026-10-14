package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive tender agreement is in its life cycle. */
enum class AdaptiveTenderAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive tender agreement, with what the planners need to act on it. */
data class AdaptiveTenderAgreement(
    var code: String,
    var title: String,
    var pallets: Int,
    var label: String,
    var shiftCode: String,
    var routeCode: String,
    var distanceKm: Double,
    var description: String,
    var ratio: Double,
    var batchSize: Int,
    var trailerId: String,
    var status: AdaptiveTenderAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTenderAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTenderAgreementStatus): Boolean {
        when (status) {
            AdaptiveTenderAgreementStatus.REQUESTED -> {
                when (next) {
                    AdaptiveTenderAgreementStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveTenderAgreementStatus.GRANTED -> {
                when (next) {
                    AdaptiveTenderAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveTenderAgreementStatus.REVOKED -> {
                when (next) {
                    AdaptiveTenderAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTenderAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tender agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedExposure(): Double = round((ratio - distanceKm) * 100) / 100

    fun snapshot(): AdaptiveTenderAgreement {
        return AdaptiveTenderAgreement(
            code = code,
            title = title,
            pallets = pallets,
            label = label,
            shiftCode = shiftCode,
            routeCode = routeCode,
            distanceKm = distanceKm,
            description = description,
            ratio = ratio,
            batchSize = batchSize,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive tender agreement in status requested. */
fun newAdaptiveTenderAgreement(code: String, title: String): AdaptiveTenderAgreement {
    return AdaptiveTenderAgreement(
        code = code,
        title = title,
        pallets = 0,
        label = "",
        shiftCode = "",
        routeCode = "",
        distanceKm = 0.0,
        description = "",
        ratio = 0.0,
        batchSize = 0,
        trailerId = "",
        status = AdaptiveTenderAgreementStatus.REQUESTED,
    )
}
