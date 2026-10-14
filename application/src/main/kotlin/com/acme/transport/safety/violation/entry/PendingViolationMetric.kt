package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending violation metric is in its life cycle. */
enum class PendingViolationMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a pending violation metric and its current status. */
data class PendingViolationMetric(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var score: Double,
    var description: String,
    var pallets: Int,
    var rate: Double,
    var status: PendingViolationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingViolationMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingViolationMetricStatus): Boolean {
        when (status) {
            PendingViolationMetricStatus.DRAFT -> {
                when (next) {
                    PendingViolationMetricStatus.PENDING, PendingViolationMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingViolationMetricStatus.PENDING -> return next == PendingViolationMetricStatus.SUSPENDED || next == PendingViolationMetricStatus.CLOSED
            PendingViolationMetricStatus.ACTIVE -> {
                when (next) {
                    PendingViolationMetricStatus.SUSPENDED, PendingViolationMetricStatus.CLOSED -> return true
                }
                return false
            }
            PendingViolationMetricStatus.SUSPENDED -> return next == PendingViolationMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingViolationMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending violation metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((pallets.toDouble() + score) * 100) / 100

    fun snapshot(): PendingViolationMetric {
        return PendingViolationMetric(
            code = code,
            name = name,
            updatedAt = updatedAt,
            score = score,
            description = description,
            pallets = pallets,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending violation metric in status draft. */
fun newPendingViolationMetric(code: String, name: String): PendingViolationMetric {
    return PendingViolationMetric(
        code = code,
        name = name,
        updatedAt = 0,
        score = 0.0,
        description = "",
        pallets = 0,
        rate = 0.0,
        status = PendingViolationMetricStatus.DRAFT,
    )
}
