package com.acme.transport.crewing.swap.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap milestone is in its life cycle. */
enum class SwapMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A swap milestone. Its code is unique within a tenant. */
data class SwapMilestone(
    var code: String,
    var label: String,
    var quantity: Int,
    var sequence: Int,
    var cost: Double,
    var distanceKm: Double,
    var minimum: Double,
    var status: SwapMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapMilestoneStatus): Boolean {
        when (status) {
            SwapMilestoneStatus.CREATED -> {
                when (next) {
                    SwapMilestoneStatus.VALIDATED, SwapMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            SwapMilestoneStatus.VALIDATED -> {
                when (next) {
                    SwapMilestoneStatus.PUBLISHED, SwapMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            SwapMilestoneStatus.PUBLISHED -> {
                when (next) {
                    SwapMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("swap milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((sequence.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): SwapMilestone {
        return SwapMilestone(
            code = code,
            label = label,
            quantity = quantity,
            sequence = sequence,
            cost = cost,
            distanceKm = distanceKm,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap milestone in status created. */
fun newSwapMilestone(code: String, label: String): SwapMilestone {
    return SwapMilestone(
        code = code,
        label = label,
        quantity = 0,
        sequence = 0,
        cost = 0.0,
        distanceKm = 0.0,
        minimum = 0.0,
        status = SwapMilestoneStatus.CREATED,
    )
}
