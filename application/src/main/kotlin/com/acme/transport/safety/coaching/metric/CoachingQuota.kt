package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching quota is in its life cycle. */
enum class CoachingQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One coaching quota, with what the planners need to act on it. */
data class CoachingQuota(
    var code: String,
    var title: String,
    var createdAt: Int,
    var summary: String,
    var locale: String,
    var minimum: Double,
    var pinned: Boolean,
    var carrier: String,
    var name: String,
    var graceDays: Int,
    var status: CoachingQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingQuotaStatus): Boolean {
        when (status) {
            CoachingQuotaStatus.CREATED -> {
                when (next) {
                    CoachingQuotaStatus.VALIDATED, CoachingQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            CoachingQuotaStatus.VALIDATED -> {
                when (next) {
                    CoachingQuotaStatus.RETIRED -> return true
                }
                return false
            }
            CoachingQuotaStatus.PUBLISHED -> {
                when (next) {
                    CoachingQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingQuotaStatus) {
        if (!canMoveTo(next)) {
            error("coaching quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((graceDays.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingQuota {
        return CoachingQuota(
            code = code,
            title = title,
            createdAt = createdAt,
            summary = summary,
            locale = locale,
            minimum = minimum,
            pinned = pinned,
            carrier = carrier,
            name = name,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a coaching quota in status created. */
fun newCoachingQuota(code: String, title: String): CoachingQuota {
    return CoachingQuota(
        code = code,
        title = title,
        createdAt = 0,
        summary = "",
        locale = "",
        minimum = 0.0,
        pinned = false,
        carrier = "",
        name = "",
        graceDays = 0,
        status = CoachingQuotaStatus.CREATED,
    )
}
