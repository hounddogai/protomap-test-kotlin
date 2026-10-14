package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled node profile is in its life cycle. */
enum class ScheduledNodeProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled node profile and its current status. */
data class ScheduledNodeProfile(
    var code: String,
    var label: String,
    var locale: String,
    var quantity: Int,
    var reviewer: String,
    var revision: Int,
    var trailerId: String,
    var segment: String,
    var attempts: Int,
    var status: ScheduledNodeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNodeProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNodeProfileStatus): Boolean {
        when (status) {
            ScheduledNodeProfileStatus.PLANNED -> return next == ScheduledNodeProfileStatus.CANCELLED
            ScheduledNodeProfileStatus.SCHEDULED -> return next == ScheduledNodeProfileStatus.IN_PROGRESS || next == ScheduledNodeProfileStatus.CANCELLED
            ScheduledNodeProfileStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledNodeProfileStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledNodeProfileStatus.COMPLETED -> {
                when (next) {
                    ScheduledNodeProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledNodeProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled node profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(quantity.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): ScheduledNodeProfile {
        return ScheduledNodeProfile(
            code = code,
            label = label,
            locale = locale,
            quantity = quantity,
            reviewer = reviewer,
            revision = revision,
            trailerId = trailerId,
            segment = segment,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled node profile in status planned. */
fun newScheduledNodeProfile(code: String, label: String): ScheduledNodeProfile {
    return ScheduledNodeProfile(
        code = code,
        label = label,
        locale = "",
        quantity = 0,
        reviewer = "",
        revision = 0,
        trailerId = "",
        segment = "",
        attempts = 0,
        status = ScheduledNodeProfileStatus.PLANNED,
    )
}
