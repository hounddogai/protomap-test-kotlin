package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.text.normalize

/** Where a capped availability preference is in its life cycle. */
enum class CappedAvailabilityPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped availability preference. Its code is unique within a tenant. */
data class CappedAvailabilityPreference(
    var code: String,
    var title: String,
    var notes: String,
    var region: String,
    var source: String,
    var routeCode: String,
    var quantity: Int,
    var status: CappedAvailabilityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAvailabilityPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAvailabilityPreferenceStatus): Boolean {
        when (status) {
            CappedAvailabilityPreferenceStatus.REQUESTED -> {
                when (next) {
                    CappedAvailabilityPreferenceStatus.GRANTED, CappedAvailabilityPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            CappedAvailabilityPreferenceStatus.GRANTED -> {
                when (next) {
                    CappedAvailabilityPreferenceStatus.REVOKED, CappedAvailabilityPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            CappedAvailabilityPreferenceStatus.REVOKED -> return next == CappedAvailabilityPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedAvailabilityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped availability preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped availability preference"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CappedAvailabilityPreference {
        return CappedAvailabilityPreference(
            code = code,
            title = title,
            notes = notes,
            region = region,
            source = source,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped availability preference in status requested. */
fun newCappedAvailabilityPreference(code: String, title: String): CappedAvailabilityPreference {
    return CappedAvailabilityPreference(
        code = code,
        title = title,
        notes = "",
        region = "",
        source = "",
        routeCode = "",
        quantity = 0,
        status = CappedAvailabilityPreferenceStatus.REQUESTED,
    )
}
