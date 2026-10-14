package com.acme.transport.customs.tariff.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff preference records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a tariff preference is in its life cycle. */
enum class TariffPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tariff preference. Its code is unique within a tenant. */
data class TariffPreference(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var minimum: Double,
    var trailerId: String,
    var driverId: String,
    var retries: Int,
    var unit: String,
    var phone: String,
    var status: TariffPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffPreferenceStatus): Boolean {
        when (status) {
            TariffPreferenceStatus.REQUESTED -> return next == TariffPreferenceStatus.REVOKED || next == TariffPreferenceStatus.EXPIRED
            TariffPreferenceStatus.GRANTED -> return next == TariffPreferenceStatus.REVOKED || next == TariffPreferenceStatus.EXPIRED
            TariffPreferenceStatus.REVOKED -> {
                when (next) {
                    TariffPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tariff preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((minimum - retries.toDouble()) * 100) / 100

    fun snapshot(): TariffPreference {
        return TariffPreference(
            code = code,
            name = name,
            pinned = pinned,
            minimum = minimum,
            trailerId = trailerId,
            driverId = driverId,
            retries = retries,
            unit = unit,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tariff preference in status requested. */
fun newTariffPreference(code: String, name: String): TariffPreference {
    return TariffPreference(
        code = code,
        name = name,
        pinned = false,
        minimum = 0.0,
        trailerId = "",
        driverId = "",
        retries = 0,
        unit = "",
        phone = "",
        status = TariffPreferenceStatus.REQUESTED,
    )
}
