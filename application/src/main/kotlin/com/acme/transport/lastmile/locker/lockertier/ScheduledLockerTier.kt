package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled locker tier records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a scheduled locker tier is in its life cycle. */
enum class ScheduledLockerTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled locker tier as the locker team keeps it. */
data class ScheduledLockerTier(
    var code: String,
    var title: String,
    var category: String,
    var externalRef: String,
    var dock: String,
    var pallets: Int,
    var etaMinutes: Int,
    var phone: String,
    var threshold: Double,
    var archived: Boolean,
    var vehicleId: String,
    var status: ScheduledLockerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerTierStatus): Boolean {
        when (status) {
            ScheduledLockerTierStatus.PROPOSED -> return next == ScheduledLockerTierStatus.REJECTED
            ScheduledLockerTierStatus.APPROVED -> return next == ScheduledLockerTierStatus.REJECTED || next == ScheduledLockerTierStatus.WITHDRAWN
            ScheduledLockerTierStatus.REJECTED -> {
                when (next) {
                    ScheduledLockerTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLockerTierStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round(pallets.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledLockerTier {
        return ScheduledLockerTier(
            code = code,
            title = title,
            category = category,
            externalRef = externalRef,
            dock = dock,
            pallets = pallets,
            etaMinutes = etaMinutes,
            phone = phone,
            threshold = threshold,
            archived = archived,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled locker tier in status proposed. */
fun newScheduledLockerTier(code: String, title: String): ScheduledLockerTier {
    return ScheduledLockerTier(
        code = code,
        title = title,
        category = "",
        externalRef = "",
        dock = "",
        pallets = 0,
        etaMinutes = 0,
        phone = "",
        threshold = 0.0,
        archived = false,
        vehicleId = "",
        status = ScheduledLockerTierStatus.PROPOSED,
    )
}
