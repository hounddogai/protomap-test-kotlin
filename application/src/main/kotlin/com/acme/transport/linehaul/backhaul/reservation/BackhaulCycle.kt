package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul cycle is in its life cycle. */
enum class BackhaulCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a backhaul cycle and its current status. */
data class BackhaulCycle(
    var code: String,
    var label: String,
    var priority: Int,
    var ratio: Double,
    var approved: Boolean,
    var region: String,
    var trailerId: String,
    var sequence: Int,
    var channel: String,
    var cost: Double,
    var status: BackhaulCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulCycleStatus): Boolean {
        when (status) {
            BackhaulCycleStatus.CREATED -> return next == BackhaulCycleStatus.PUBLISHED
            BackhaulCycleStatus.VALIDATED -> return next == BackhaulCycleStatus.PUBLISHED || next == BackhaulCycleStatus.RETIRED
            BackhaulCycleStatus.PUBLISHED -> return next == BackhaulCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BackhaulCycleStatus) {
        if (!canMoveTo(next)) {
            error("backhaul cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round(sequence.toDouble() * ratio * 100) / 100

    fun snapshot(): BackhaulCycle {
        return BackhaulCycle(
            code = code,
            label = label,
            priority = priority,
            ratio = ratio,
            approved = approved,
            region = region,
            trailerId = trailerId,
            sequence = sequence,
            channel = channel,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a backhaul cycle in status created. */
fun newBackhaulCycle(code: String, label: String): BackhaulCycle {
    return BackhaulCycle(
        code = code,
        label = label,
        priority = 0,
        ratio = 0.0,
        approved = false,
        region = "",
        trailerId = "",
        sequence = 0,
        channel = "",
        cost = 0.0,
        status = BackhaulCycleStatus.CREATED,
    )
}
