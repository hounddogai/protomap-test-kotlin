package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service area quota is in its life cycle. */
enum class StrictServiceAreaQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict service area quota as the service area team keeps it. */
data class StrictServiceAreaQuota(
    var code: String,
    var name: String,
    var attempts: Int,
    var contactEmail: String,
    var distanceKm: Double,
    var windowDays: Int,
    var budget: Double,
    var expiresAt: Int,
    var pinned: Boolean,
    var status: StrictServiceAreaQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaQuotaStatus): Boolean {
        when (status) {
            StrictServiceAreaQuotaStatus.IDLE -> {
                when (next) {
                    StrictServiceAreaQuotaStatus.WARMING, StrictServiceAreaQuotaStatus.STOPPED -> return true
                }
                return false
            }
            StrictServiceAreaQuotaStatus.WARMING -> {
                when (next) {
                    StrictServiceAreaQuotaStatus.DRAINING -> return true
                }
                return false
            }
            StrictServiceAreaQuotaStatus.READY -> {
                when (next) {
                    StrictServiceAreaQuotaStatus.DRAINING, StrictServiceAreaQuotaStatus.STOPPED -> return true
                }
                return false
            }
            StrictServiceAreaQuotaStatus.DRAINING -> return next == StrictServiceAreaQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaQuotaStatus) {
        if (!canMoveTo(next)) {
            error("strict service area quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((distanceKm - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictServiceAreaQuota {
        return StrictServiceAreaQuota(
            code = code,
            name = name,
            attempts = attempts,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            windowDays = windowDays,
            budget = budget,
            expiresAt = expiresAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict service area quota in status idle. */
fun newStrictServiceAreaQuota(code: String, name: String): StrictServiceAreaQuota {
    return StrictServiceAreaQuota(
        code = code,
        name = name,
        attempts = 0,
        contactEmail = "",
        distanceKm = 0.0,
        windowDays = 0,
        budget = 0.0,
        expiresAt = 0,
        pinned = false,
        status = StrictServiceAreaQuotaStatus.IDLE,
    )
}
