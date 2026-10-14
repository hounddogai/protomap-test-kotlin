package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling rate card entry is in its life cycle. */
enum class RollingRateCardEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling rate card entry as the rate card team keeps it. */
data class RollingRateCardEntry(
    var code: String,
    var title: String,
    var category: String,
    var odometer: Int,
    var externalRef: String,
    var floorValue: Double,
    var endsAt: Int,
    var distanceKm: Double,
    var expiresAt: Int,
    var contactEmail: String,
    var capacity: Int,
    var status: RollingRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardEntryStatus): Boolean {
        when (status) {
            RollingRateCardEntryStatus.PROPOSED -> return next == RollingRateCardEntryStatus.REJECTED
            RollingRateCardEntryStatus.APPROVED -> {
                when (next) {
                    RollingRateCardEntryStatus.REJECTED -> return true
                }
                return false
            }
            RollingRateCardEntryStatus.REJECTED -> {
                when (next) {
                    RollingRateCardEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((endsAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): RollingRateCardEntry {
        return RollingRateCardEntry(
            code = code,
            title = title,
            category = category,
            odometer = odometer,
            externalRef = externalRef,
            floorValue = floorValue,
            endsAt = endsAt,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling rate card entry in status proposed. */
fun newRollingRateCardEntry(code: String, title: String): RollingRateCardEntry {
    return RollingRateCardEntry(
        code = code,
        title = title,
        category = "",
        odometer = 0,
        externalRef = "",
        floorValue = 0.0,
        endsAt = 0,
        distanceKm = 0.0,
        expiresAt = 0,
        contactEmail = "",
        capacity = 0,
        status = RollingRateCardEntryStatus.PROPOSED,
    )
}
