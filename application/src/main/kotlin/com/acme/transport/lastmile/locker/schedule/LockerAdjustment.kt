package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker adjustment is in its life cycle. */
enum class LockerAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A locker adjustment as the locker team keeps it. */
data class LockerAdjustment(
    var code: String,
    var name: String,
    var contactEmail: String,
    var endsAt: Int,
    var startsAt: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var channel: String,
    var retries: Int,
    var quantity: Int,
    var revision: Int,
    var status: LockerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerAdjustmentStatus): Boolean {
        when (status) {
            LockerAdjustmentStatus.REQUESTED -> {
                when (next) {
                    LockerAdjustmentStatus.GRANTED -> return true
                }
                return false
            }
            LockerAdjustmentStatus.GRANTED -> {
                when (next) {
                    LockerAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            LockerAdjustmentStatus.REVOKED -> {
                when (next) {
                    LockerAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("locker adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((quantity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LockerAdjustment {
        return LockerAdjustment(
            code = code,
            name = name,
            contactEmail = contactEmail,
            endsAt = endsAt,
            startsAt = startsAt,
            pinned = pinned,
            vehicleId = vehicleId,
            channel = channel,
            retries = retries,
            quantity = quantity,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a locker adjustment in status requested. */
fun newLockerAdjustment(code: String, name: String): LockerAdjustment {
    return LockerAdjustment(
        code = code,
        name = name,
        contactEmail = "",
        endsAt = 0,
        startsAt = 0,
        pinned = false,
        vehicleId = "",
        channel = "",
        retries = 0,
        quantity = 0,
        revision = 0,
        status = LockerAdjustmentStatus.REQUESTED,
    )
}
