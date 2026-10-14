package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel burn profile is in its life cycle. */
enum class LenientFuelBurnProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lenient fuel burn profile and its current status. */
data class LenientFuelBurnProfile(
    var code: String,
    var title: String,
    var country: String,
    var locale: String,
    var depot: String,
    var startsAt: Int,
    var fuelLiters: Double,
    var status: LenientFuelBurnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelBurnProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelBurnProfileStatus): Boolean {
        when (status) {
            LenientFuelBurnProfileStatus.PROPOSED -> {
                when (next) {
                    LenientFuelBurnProfileStatus.REJECTED, LenientFuelBurnProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientFuelBurnProfileStatus.APPROVED -> {
                when (next) {
                    LenientFuelBurnProfileStatus.REJECTED, LenientFuelBurnProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientFuelBurnProfileStatus.REJECTED -> {
                when (next) {
                    LenientFuelBurnProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFuelBurnProfileStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel burn profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient fuel burn profile"} $code"

    fun effectiveVolume(): Double = round((fuelLiters + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientFuelBurnProfile {
        return LenientFuelBurnProfile(
            code = code,
            title = title,
            country = country,
            locale = locale,
            depot = depot,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient fuel burn profile in status proposed. */
fun newLenientFuelBurnProfile(code: String, title: String): LenientFuelBurnProfile {
    return LenientFuelBurnProfile(
        code = code,
        title = title,
        country = "",
        locale = "",
        depot = "",
        startsAt = 0,
        fuelLiters = 0.0,
        status = LenientFuelBurnProfileStatus.PROPOSED,
    )
}
