package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient availability tier records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a lenient availability tier is in its life cycle. */
enum class LenientAvailabilityTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient availability tier and its current status. */
data class LenientAvailabilityTier(
    var code: String,
    var name: String,
    var approved: Boolean,
    var title: String,
    var limit: Int,
    var createdAt: Int,
    var windowDays: Int,
    var updatedAt: Int,
    var status: LenientAvailabilityTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAvailabilityTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAvailabilityTierStatus): Boolean {
        when (status) {
            LenientAvailabilityTierStatus.CREATED -> {
                when (next) {
                    LenientAvailabilityTierStatus.VALIDATED -> return true
                }
                return false
            }
            LenientAvailabilityTierStatus.VALIDATED -> {
                when (next) {
                    LenientAvailabilityTierStatus.PUBLISHED, LenientAvailabilityTierStatus.RETIRED -> return true
                }
                return false
            }
            LenientAvailabilityTierStatus.PUBLISHED -> {
                when (next) {
                    LenientAvailabilityTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAvailabilityTierStatus) {
        if (!canMoveTo(next)) {
            error("lenient availability tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((createdAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientAvailabilityTier {
        return LenientAvailabilityTier(
            code = code,
            name = name,
            approved = approved,
            title = title,
            limit = limit,
            createdAt = createdAt,
            windowDays = windowDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient availability tier in status created. */
fun newLenientAvailabilityTier(code: String, name: String): LenientAvailabilityTier {
    return LenientAvailabilityTier(
        code = code,
        name = name,
        approved = false,
        title = "",
        limit = 0,
        createdAt = 0,
        windowDays = 0,
        updatedAt = 0,
        status = LenientAvailabilityTierStatus.CREATED,
    )
}
