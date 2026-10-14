package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel surcharge preference is in its life cycle. */
enum class RollingFuelSurchargePreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling fuel surcharge preference, with what the planners need to act on it. */
data class RollingFuelSurchargePreference(
    var code: String,
    var title: String,
    var score: Double,
    var distanceKm: Double,
    var stopCount: Int,
    var quantity: Int,
    var status: RollingFuelSurchargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelSurchargePreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelSurchargePreferenceStatus): Boolean {
        when (status) {
            RollingFuelSurchargePreferenceStatus.OPEN -> {
                when (next) {
                    RollingFuelSurchargePreferenceStatus.IN_REVIEW, RollingFuelSurchargePreferenceStatus.RESOLVED -> return true
                }
                return false
            }
            RollingFuelSurchargePreferenceStatus.IN_REVIEW -> {
                when (next) {
                    RollingFuelSurchargePreferenceStatus.RESOLVED -> return true
                }
                return false
            }
            RollingFuelSurchargePreferenceStatus.RESOLVED -> return next == RollingFuelSurchargePreferenceStatus.REOPENED || next == RollingFuelSurchargePreferenceStatus.ARCHIVED
            RollingFuelSurchargePreferenceStatus.REOPENED -> return next == RollingFuelSurchargePreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingFuelSurchargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel surcharge preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((quantity.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingFuelSurchargePreference {
        return RollingFuelSurchargePreference(
            code = code,
            title = title,
            score = score,
            distanceKm = distanceKm,
            stopCount = stopCount,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling fuel surcharge preference in status open. */
fun newRollingFuelSurchargePreference(code: String, title: String): RollingFuelSurchargePreference {
    return RollingFuelSurchargePreference(
        code = code,
        title = title,
        score = 0.0,
        distanceKm = 0.0,
        stopCount = 0,
        quantity = 0,
        status = RollingFuelSurchargePreferenceStatus.OPEN,
    )
}
