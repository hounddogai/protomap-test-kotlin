package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching metric is in its life cycle. */
enum class DefaultCoachingMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default coaching metric, with what the planners need to act on it. */
data class DefaultCoachingMetric(
    var code: String,
    var name: String,
    var unit: String,
    var amount: Double,
    var rate: Double,
    var checksum: String,
    var country: String,
    var version: Int,
    var summary: String,
    var archived: Boolean,
    var status: DefaultCoachingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingMetricStatus): Boolean {
        when (status) {
            DefaultCoachingMetricStatus.REQUESTED -> {
                when (next) {
                    DefaultCoachingMetricStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultCoachingMetricStatus.GRANTED -> return next == DefaultCoachingMetricStatus.EXPIRED
            DefaultCoachingMetricStatus.REVOKED -> return next == DefaultCoachingMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultCoachingMetricStatus) {
        if (!canMoveTo(next)) {
            error("default coaching metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((rate + version.toDouble()) * 100) / 100

    fun snapshot(): DefaultCoachingMetric {
        return DefaultCoachingMetric(
            code = code,
            name = name,
            unit = unit,
            amount = amount,
            rate = rate,
            checksum = checksum,
            country = country,
            version = version,
            summary = summary,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default coaching metric in status requested. */
fun newDefaultCoachingMetric(code: String, name: String): DefaultCoachingMetric {
    return DefaultCoachingMetric(
        code = code,
        name = name,
        unit = "",
        amount = 0.0,
        rate = 0.0,
        checksum = "",
        country = "",
        version = 0,
        summary = "",
        archived = false,
        status = DefaultCoachingMetricStatus.REQUESTED,
    )
}
