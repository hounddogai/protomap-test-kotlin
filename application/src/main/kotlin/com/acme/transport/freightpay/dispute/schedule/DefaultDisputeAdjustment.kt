package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dispute adjustment is in its life cycle. */
enum class DefaultDisputeAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default dispute adjustment and its current status. */
data class DefaultDisputeAdjustment(
    var code: String,
    var name: String,
    var margin: Double,
    var minimum: Double,
    var maximum: Double,
    var dock: String,
    var category: String,
    var revision: Int,
    var attempts: Int,
    var status: DefaultDisputeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDisputeAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDisputeAdjustmentStatus): Boolean {
        when (status) {
            DefaultDisputeAdjustmentStatus.REQUESTED -> {
                when (next) {
                    DefaultDisputeAdjustmentStatus.GRANTED -> return true
                }
                return false
            }
            DefaultDisputeAdjustmentStatus.GRANTED -> return next == DefaultDisputeAdjustmentStatus.REVOKED
            DefaultDisputeAdjustmentStatus.REVOKED -> return next == DefaultDisputeAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDisputeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("default dispute adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"default dispute adjustment"} $code"

    fun projectedValue(): Double = round(revision.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): DefaultDisputeAdjustment {
        return DefaultDisputeAdjustment(
            code = code,
            name = name,
            margin = margin,
            minimum = minimum,
            maximum = maximum,
            dock = dock,
            category = category,
            revision = revision,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default dispute adjustment in status requested. */
fun newDefaultDisputeAdjustment(code: String, name: String): DefaultDisputeAdjustment {
    return DefaultDisputeAdjustment(
        code = code,
        name = name,
        margin = 0.0,
        minimum = 0.0,
        maximum = 0.0,
        dock = "",
        category = "",
        revision = 0,
        attempts = 0,
        status = DefaultDisputeAdjustmentStatus.REQUESTED,
    )
}
