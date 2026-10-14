package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default audit baseline is in its life cycle. */
enum class DefaultAuditBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default audit baseline and its current status. */
data class DefaultAuditBaseline(
    var code: String,
    var title: String,
    var priority: Int,
    var label: String,
    var fuelLiters: Double,
    var lane: String,
    var archived: Boolean,
    var source: String,
    var weight: Double,
    var summary: String,
    var trailerId: String,
    var status: DefaultAuditBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAuditBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAuditBaselineStatus): Boolean {
        when (status) {
            DefaultAuditBaselineStatus.IDLE -> return next == DefaultAuditBaselineStatus.DRAINING
            DefaultAuditBaselineStatus.WARMING -> {
                when (next) {
                    DefaultAuditBaselineStatus.DRAINING, DefaultAuditBaselineStatus.STOPPED -> return true
                }
                return false
            }
            DefaultAuditBaselineStatus.READY -> {
                when (next) {
                    DefaultAuditBaselineStatus.DRAINING -> return true
                }
                return false
            }
            DefaultAuditBaselineStatus.DRAINING -> return next == DefaultAuditBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultAuditBaselineStatus) {
        if (!canMoveTo(next)) {
            error("default audit baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default audit baseline"} $code"

    fun effectiveExposure(): Double = round((weight + fuelLiters) * 100) / 100

    fun snapshot(): DefaultAuditBaseline {
        return DefaultAuditBaseline(
            code = code,
            title = title,
            priority = priority,
            label = label,
            fuelLiters = fuelLiters,
            lane = lane,
            archived = archived,
            source = source,
            weight = weight,
            summary = summary,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default audit baseline in status idle. */
fun newDefaultAuditBaseline(code: String, title: String): DefaultAuditBaseline {
    return DefaultAuditBaseline(
        code = code,
        title = title,
        priority = 0,
        label = "",
        fuelLiters = 0.0,
        lane = "",
        archived = false,
        source = "",
        weight = 0.0,
        summary = "",
        trailerId = "",
        status = DefaultAuditBaselineStatus.IDLE,
    )
}
