package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff profile records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff profile is in its life cycle. */
enum class TariffProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tariff profile. Its code is unique within a tenant. */
data class TariffProfile(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var stopCount: Int,
    var weight: Double,
    var quantity: Int,
    var startsAt: Int,
    var active: Boolean,
    var status: TariffProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffProfileStatus): Boolean {
        when (status) {
            TariffProfileStatus.OPEN -> return next == TariffProfileStatus.REOPENED
            TariffProfileStatus.IN_REVIEW -> {
                when (next) {
                    TariffProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            TariffProfileStatus.RESOLVED -> {
                when (next) {
                    TariffProfileStatus.REOPENED, TariffProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            TariffProfileStatus.REOPENED -> {
                when (next) {
                    TariffProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffProfileStatus) {
        if (!canMoveTo(next)) {
            error("tariff profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round(stopCount.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): TariffProfile {
        return TariffProfile(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            weight = weight,
            quantity = quantity,
            startsAt = startsAt,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tariff profile in status open. */
fun newTariffProfile(code: String, label: String): TariffProfile {
    return TariffProfile(
        code = code,
        label = label,
        durationMinutes = 0,
        stopCount = 0,
        weight = 0.0,
        quantity = 0,
        startsAt = 0,
        active = false,
        status = TariffProfileStatus.OPEN,
    )
}
