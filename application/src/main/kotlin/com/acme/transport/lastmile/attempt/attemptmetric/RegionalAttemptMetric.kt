package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional attempt metric records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a regional attempt metric is in its life cycle. */
enum class RegionalAttemptMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional attempt metric and its current status. */
data class RegionalAttemptMetric(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var archived: Boolean,
    var tags: MutableList<String>,
    var locale: String,
    var cost: Double,
    var unit: String,
    var discount: Double,
    var status: RegionalAttemptMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAttemptMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAttemptMetricStatus): Boolean {
        when (status) {
            RegionalAttemptMetricStatus.REQUESTED -> return next == RegionalAttemptMetricStatus.REVOKED
            RegionalAttemptMetricStatus.GRANTED -> {
                when (next) {
                    RegionalAttemptMetricStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalAttemptMetricStatus.REVOKED -> return next == RegionalAttemptMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalAttemptMetricStatus) {
        if (!canMoveTo(next)) {
            error("regional attempt metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round(cost * discount * 100) / 100

    fun snapshot(): RegionalAttemptMetric {
        return RegionalAttemptMetric(
            code = code,
            title = title,
            pinned = pinned,
            archived = archived,
            tags = tags,
            locale = locale,
            cost = cost,
            unit = unit,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional attempt metric in status requested. */
fun newRegionalAttemptMetric(code: String, title: String): RegionalAttemptMetric {
    return RegionalAttemptMetric(
        code = code,
        title = title,
        pinned = false,
        archived = false,
        tags = mutableListOf<String>(),
        locale = "",
        cost = 0.0,
        unit = "",
        discount = 0.0,
        status = RegionalAttemptMetricStatus.REQUESTED,
    )
}
