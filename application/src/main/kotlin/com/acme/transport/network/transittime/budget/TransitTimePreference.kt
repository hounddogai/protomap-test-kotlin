package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time preference is in its life cycle. */
enum class TransitTimePreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A transit time preference. Its code is unique within a tenant. */
data class TransitTimePreference(
    var code: String,
    var name: String,
    var contactEmail: String,
    var endsAt: Int,
    var durationMinutes: Int,
    var etaMinutes: Int,
    var retries: Int,
    var currency: String,
    var status: TransitTimePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimePreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimePreferenceStatus): Boolean {
        when (status) {
            TransitTimePreferenceStatus.DRAFT -> return next == TransitTimePreferenceStatus.ACTIVE || next == TransitTimePreferenceStatus.CLOSED
            TransitTimePreferenceStatus.PENDING -> {
                when (next) {
                    TransitTimePreferenceStatus.ACTIVE, TransitTimePreferenceStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimePreferenceStatus.ACTIVE -> return next == TransitTimePreferenceStatus.CLOSED
            TransitTimePreferenceStatus.SUSPENDED -> {
                when (next) {
                    TransitTimePreferenceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("transit time preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedExposure(): Double = round((durationMinutes.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): TransitTimePreference {
        return TransitTimePreference(
            code = code,
            name = name,
            contactEmail = contactEmail,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            etaMinutes = etaMinutes,
            retries = retries,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time preference in status draft. */
fun newTransitTimePreference(code: String, name: String): TransitTimePreference {
    return TransitTimePreference(
        code = code,
        name = name,
        contactEmail = "",
        endsAt = 0,
        durationMinutes = 0,
        etaMinutes = 0,
        retries = 0,
        currency = "",
        status = TransitTimePreferenceStatus.DRAFT,
    )
}
