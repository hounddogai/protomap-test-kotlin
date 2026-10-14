package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity profile is in its life cycle. */
enum class RegionalCapacityProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional capacity profile. Its code is unique within a tenant. */
data class RegionalCapacityProfile(
    var code: String,
    var name: String,
    var label: String,
    var contactEmail: String,
    var endsAt: Int,
    var archived: Boolean,
    var vehicleId: String,
    var category: String,
    var ceiling: Double,
    var status: RegionalCapacityProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityProfileStatus): Boolean {
        when (status) {
            RegionalCapacityProfileStatus.REQUESTED -> {
                when (next) {
                    RegionalCapacityProfileStatus.REVOKED -> return true
                }
                return false
            }
            RegionalCapacityProfileStatus.GRANTED -> return next == RegionalCapacityProfileStatus.EXPIRED
            RegionalCapacityProfileStatus.REVOKED -> return next == RegionalCapacityProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun projectedVolume(): Double = round((ceiling + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalCapacityProfile {
        return RegionalCapacityProfile(
            code = code,
            name = name,
            label = label,
            contactEmail = contactEmail,
            endsAt = endsAt,
            archived = archived,
            vehicleId = vehicleId,
            category = category,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional capacity profile in status requested. */
fun newRegionalCapacityProfile(code: String, name: String): RegionalCapacityProfile {
    return RegionalCapacityProfile(
        code = code,
        name = name,
        label = "",
        contactEmail = "",
        endsAt = 0,
        archived = false,
        vehicleId = "",
        category = "",
        ceiling = 0.0,
        status = RegionalCapacityProfileStatus.REQUESTED,
    )
}
