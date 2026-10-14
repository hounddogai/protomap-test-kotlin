package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cutoff target is in its life cycle. */
enum class DefaultCutoffTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default cutoff target. Its code is unique within a tenant. */
data class DefaultCutoffTarget(
    var code: String,
    var label: String,
    var currency: String,
    var priority: Int,
    var updatedAt: Int,
    var limit: Int,
    var threshold: Double,
    var depot: String,
    var startsAt: Int,
    var externalRef: String,
    var status: DefaultCutoffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffTargetStatus): Boolean {
        when (status) {
            DefaultCutoffTargetStatus.IDLE -> return next == DefaultCutoffTargetStatus.WARMING || next == DefaultCutoffTargetStatus.DRAINING
            DefaultCutoffTargetStatus.WARMING -> return next == DefaultCutoffTargetStatus.STOPPED
            DefaultCutoffTargetStatus.READY -> return next == DefaultCutoffTargetStatus.STOPPED
            DefaultCutoffTargetStatus.DRAINING -> return next == DefaultCutoffTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultCutoffTargetStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round(priority.toDouble() * threshold * 100) / 100

    fun snapshot(): DefaultCutoffTarget {
        return DefaultCutoffTarget(
            code = code,
            label = label,
            currency = currency,
            priority = priority,
            updatedAt = updatedAt,
            limit = limit,
            threshold = threshold,
            depot = depot,
            startsAt = startsAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default cutoff target in status idle. */
fun newDefaultCutoffTarget(code: String, label: String): DefaultCutoffTarget {
    return DefaultCutoffTarget(
        code = code,
        label = label,
        currency = "",
        priority = 0,
        updatedAt = 0,
        limit = 0,
        threshold = 0.0,
        depot = "",
        startsAt = 0,
        externalRef = "",
        status = DefaultCutoffTargetStatus.IDLE,
    )
}
