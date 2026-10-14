package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled tire bundle is in its life cycle. */
enum class ScheduledTireBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled tire bundle as the tire team keeps it. */
data class ScheduledTireBundle(
    var code: String,
    var label: String,
    var minimum: Double,
    var name: String,
    var updatedAt: Int,
    var floorValue: Double,
    var contactEmail: String,
    var status: ScheduledTireBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTireBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTireBundleStatus): Boolean {
        when (status) {
            ScheduledTireBundleStatus.REQUESTED -> {
                when (next) {
                    ScheduledTireBundleStatus.GRANTED -> return true
                }
                return false
            }
            ScheduledTireBundleStatus.GRANTED -> {
                when (next) {
                    ScheduledTireBundleStatus.REVOKED, ScheduledTireBundleStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledTireBundleStatus.REVOKED -> {
                when (next) {
                    ScheduledTireBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTireBundleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tire bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round(updatedAt.toDouble() * minimum * 100) / 100

    fun snapshot(): ScheduledTireBundle {
        return ScheduledTireBundle(
            code = code,
            label = label,
            minimum = minimum,
            name = name,
            updatedAt = updatedAt,
            floorValue = floorValue,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled tire bundle in status requested. */
fun newScheduledTireBundle(code: String, label: String): ScheduledTireBundle {
    return ScheduledTireBundle(
        code = code,
        label = label,
        minimum = 0.0,
        name = "",
        updatedAt = 0,
        floorValue = 0.0,
        contactEmail = "",
        status = ScheduledTireBundleStatus.REQUESTED,
    )
}
