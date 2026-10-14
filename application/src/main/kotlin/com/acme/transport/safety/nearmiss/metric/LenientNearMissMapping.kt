package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient near miss mapping is in its life cycle. */
enum class LenientNearMissMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient near miss mapping and its current status. */
data class LenientNearMissMapping(
    var code: String,
    var name: String,
    var country: String,
    var pinned: Boolean,
    var budget: Double,
    var expiresAt: Int,
    var graceDays: Int,
    var status: LenientNearMissMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissMappingStatus): Boolean {
        when (status) {
            LenientNearMissMappingStatus.OPEN -> return next == LenientNearMissMappingStatus.REOPENED || next == LenientNearMissMappingStatus.ARCHIVED
            LenientNearMissMappingStatus.IN_REVIEW -> {
                when (next) {
                    LenientNearMissMappingStatus.REOPENED -> return true
                }
                return false
            }
            LenientNearMissMappingStatus.RESOLVED -> return next == LenientNearMissMappingStatus.ARCHIVED
            LenientNearMissMappingStatus.REOPENED -> return next == LenientNearMissMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientNearMissMappingStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient near miss mapping"} $code"

    fun effectiveExposure(): Double = round(budget * expiresAt.toDouble() * 100) / 100

    fun snapshot(): LenientNearMissMapping {
        return LenientNearMissMapping(
            code = code,
            name = name,
            country = country,
            pinned = pinned,
            budget = budget,
            expiresAt = expiresAt,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient near miss mapping in status open. */
fun newLenientNearMissMapping(code: String, name: String): LenientNearMissMapping {
    return LenientNearMissMapping(
        code = code,
        name = name,
        country = "",
        pinned = false,
        budget = 0.0,
        expiresAt = 0,
        graceDays = 0,
        status = LenientNearMissMappingStatus.OPEN,
    )
}
