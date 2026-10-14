package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap assignment is in its life cycle. */
enum class SwapAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One swap assignment, with what the planners need to act on it. */
data class SwapAssignment(
    var code: String,
    var label: String,
    var externalRef: String,
    var active: Boolean,
    var pinned: Boolean,
    var createdAt: Int,
    var quantity: Int,
    var fuelLiters: Double,
    var status: SwapAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapAssignmentStatus): Boolean {
        when (status) {
            SwapAssignmentStatus.CREATED -> {
                when (next) {
                    SwapAssignmentStatus.PUBLISHED, SwapAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            SwapAssignmentStatus.VALIDATED -> {
                when (next) {
                    SwapAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            SwapAssignmentStatus.PUBLISHED -> {
                when (next) {
                    SwapAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("swap assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((createdAt.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): SwapAssignment {
        return SwapAssignment(
            code = code,
            label = label,
            externalRef = externalRef,
            active = active,
            pinned = pinned,
            createdAt = createdAt,
            quantity = quantity,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap assignment in status created. */
fun newSwapAssignment(code: String, label: String): SwapAssignment {
    return SwapAssignment(
        code = code,
        label = label,
        externalRef = "",
        active = false,
        pinned = false,
        createdAt = 0,
        quantity = 0,
        fuelLiters = 0.0,
        status = SwapAssignmentStatus.CREATED,
    )
}
