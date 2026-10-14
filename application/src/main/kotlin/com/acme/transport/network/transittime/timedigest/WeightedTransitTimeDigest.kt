package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted transit time digest records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a weighted transit time digest is in its life cycle. */
enum class WeightedTransitTimeDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted transit time digest. Its code is unique within a tenant. */
data class WeightedTransitTimeDigest(
    var code: String,
    var title: String,
    var graceDays: Int,
    var depot: String,
    var locale: String,
    var createdAt: Int,
    var rate: Double,
    var reviewer: String,
    var region: String,
    var name: String,
    var status: WeightedTransitTimeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTransitTimeDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTransitTimeDigestStatus): Boolean {
        when (status) {
            WeightedTransitTimeDigestStatus.PLANNED -> return next == WeightedTransitTimeDigestStatus.SCHEDULED
            WeightedTransitTimeDigestStatus.SCHEDULED -> {
                when (next) {
                    WeightedTransitTimeDigestStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTransitTimeDigestStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedTransitTimeDigestStatus.COMPLETED, WeightedTransitTimeDigestStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTransitTimeDigestStatus.COMPLETED -> return next == WeightedTransitTimeDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedTransitTimeDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted transit time digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round((rate + createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedTransitTimeDigest {
        return WeightedTransitTimeDigest(
            code = code,
            title = title,
            graceDays = graceDays,
            depot = depot,
            locale = locale,
            createdAt = createdAt,
            rate = rate,
            reviewer = reviewer,
            region = region,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted transit time digest in status planned. */
fun newWeightedTransitTimeDigest(code: String, title: String): WeightedTransitTimeDigest {
    return WeightedTransitTimeDigest(
        code = code,
        title = title,
        graceDays = 0,
        depot = "",
        locale = "",
        createdAt = 0,
        rate = 0.0,
        reviewer = "",
        region = "",
        name = "",
        status = WeightedTransitTimeDigestStatus.PLANNED,
    )
}
