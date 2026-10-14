package com.acme.transport.network.zone.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global zone review records of network's zone work, and the rules, stores, and calculations around them.

/** Where a global zone review is in its life cycle. */
enum class GlobalZoneReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global zone review. Its code is unique within a tenant. */
data class GlobalZoneReview(
    var code: String,
    var title: String,
    var externalRef: String,
    var quantity: Int,
    var country: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var vehicleId: String,
    var shiftCode: String,
    var approved: Boolean,
    var status: GlobalZoneReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneReviewStatus): Boolean {
        when (status) {
            GlobalZoneReviewStatus.PROPOSED -> return next == GlobalZoneReviewStatus.APPROVED || next == GlobalZoneReviewStatus.REJECTED
            GlobalZoneReviewStatus.APPROVED -> return next == GlobalZoneReviewStatus.REJECTED || next == GlobalZoneReviewStatus.WITHDRAWN
            GlobalZoneReviewStatus.REJECTED -> {
                when (next) {
                    GlobalZoneReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalZoneReviewStatus) {
        if (!canMoveTo(next)) {
            error("global zone review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round(durationMinutes.toDouble() * loadKg * 100) / 100

    fun snapshot(): GlobalZoneReview {
        return GlobalZoneReview(
            code = code,
            title = title,
            externalRef = externalRef,
            quantity = quantity,
            country = country,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            vehicleId = vehicleId,
            shiftCode = shiftCode,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global zone review in status proposed. */
fun newGlobalZoneReview(code: String, title: String): GlobalZoneReview {
    return GlobalZoneReview(
        code = code,
        title = title,
        externalRef = "",
        quantity = 0,
        country = "",
        loadKg = 0.0,
        durationMinutes = 0,
        vehicleId = "",
        shiftCode = "",
        approved = false,
        status = GlobalZoneReviewStatus.PROPOSED,
    )
}
