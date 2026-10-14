package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching tier records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching tier is in its life cycle. */
enum class CoachingTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A coaching tier. Its code is unique within a tenant. */
data class CoachingTier(
    var code: String,
    var title: String,
    var quantity: Int,
    var revision: Int,
    var locale: String,
    var reviewer: String,
    var maximum: Double,
    var status: CoachingTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingTierStatus): Boolean {
        when (status) {
            CoachingTierStatus.DRAFT -> return next == CoachingTierStatus.PENDING
            CoachingTierStatus.PENDING -> return next == CoachingTierStatus.SUSPENDED || next == CoachingTierStatus.CLOSED
            CoachingTierStatus.ACTIVE -> {
                when (next) {
                    CoachingTierStatus.CLOSED -> return true
                }
                return false
            }
            CoachingTierStatus.SUSPENDED -> {
                when (next) {
                    CoachingTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingTierStatus) {
        if (!canMoveTo(next)) {
            error("coaching tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"coaching tier"} $code"

    fun adjustedVolume(): Double = round((quantity.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): CoachingTier {
        return CoachingTier(
            code = code,
            title = title,
            quantity = quantity,
            revision = revision,
            locale = locale,
            reviewer = reviewer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a coaching tier in status draft. */
fun newCoachingTier(code: String, title: String): CoachingTier {
    return CoachingTier(
        code = code,
        title = title,
        quantity = 0,
        revision = 0,
        locale = "",
        reviewer = "",
        maximum = 0.0,
        status = CoachingTierStatus.DRAFT,
    )
}
