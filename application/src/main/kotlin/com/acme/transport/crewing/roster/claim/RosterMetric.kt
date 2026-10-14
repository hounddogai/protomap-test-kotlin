package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster metric is in its life cycle. */
enum class RosterMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A roster metric. Its code is unique within a tenant. */
data class RosterMetric(
    var code: String,
    var name: String,
    var threshold: Double,
    var limit: Int,
    var distanceKm: Double,
    var capacity: Int,
    var description: String,
    var attempts: Int,
    var sequence: Int,
    var status: RosterMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterMetricStatus): Boolean {
        when (status) {
            RosterMetricStatus.DRAFT -> return next == RosterMetricStatus.PENDING || next == RosterMetricStatus.SUSPENDED
            RosterMetricStatus.PENDING -> return next == RosterMetricStatus.ACTIVE || next == RosterMetricStatus.CLOSED
            RosterMetricStatus.ACTIVE -> {
                when (next) {
                    RosterMetricStatus.CLOSED -> return true
                }
                return false
            }
            RosterMetricStatus.SUSPENDED -> {
                when (next) {
                    RosterMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterMetricStatus) {
        if (!canMoveTo(next)) {
            error("roster metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster metric"} $code"

    fun netTotal(): Double = round((distanceKm - limit.toDouble()) * 100) / 100

    fun snapshot(): RosterMetric {
        return RosterMetric(
            code = code,
            name = name,
            threshold = threshold,
            limit = limit,
            distanceKm = distanceKm,
            capacity = capacity,
            description = description,
            attempts = attempts,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster metric in status draft. */
fun newRosterMetric(code: String, name: String): RosterMetric {
    return RosterMetric(
        code = code,
        name = name,
        threshold = 0.0,
        limit = 0,
        distanceKm = 0.0,
        capacity = 0,
        description = "",
        attempts = 0,
        sequence = 0,
        status = RosterMetricStatus.DRAFT,
    )
}
