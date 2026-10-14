package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.text.normalize

/** Where a regional capacity preference is in its life cycle. */
enum class RegionalCapacityPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional capacity preference, with what the planners need to act on it. */
data class RegionalCapacityPreference(
    var code: String,
    var name: String,
    var checksum: String,
    var active: Boolean,
    var segment: String,
    var startsAt: Int,
    var status: RegionalCapacityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityPreferenceStatus): Boolean {
        when (status) {
            RegionalCapacityPreferenceStatus.REQUESTED -> return next == RegionalCapacityPreferenceStatus.GRANTED
            RegionalCapacityPreferenceStatus.GRANTED -> return next == RegionalCapacityPreferenceStatus.EXPIRED
            RegionalCapacityPreferenceStatus.REVOKED -> return next == RegionalCapacityPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): RegionalCapacityPreference {
        return RegionalCapacityPreference(
            code = code,
            name = name,
            checksum = checksum,
            active = active,
            segment = segment,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional capacity preference in status requested. */
fun newRegionalCapacityPreference(code: String, name: String): RegionalCapacityPreference {
    return RegionalCapacityPreference(
        code = code,
        name = name,
        checksum = "",
        active = false,
        segment = "",
        startsAt = 0,
        status = RegionalCapacityPreferenceStatus.REQUESTED,
    )
}
