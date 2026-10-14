package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dock door claim is in its life cycle. */
enum class WeightedDockDoorClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a weighted dock door claim and its current status. */
data class WeightedDockDoorClaim(
    var code: String,
    var label: String,
    var maximum: Double,
    var quantity: Int,
    var checksum: String,
    var carrier: String,
    var archived: Boolean,
    var minimum: Double,
    var shiftCode: String,
    var updatedAt: Int,
    var status: WeightedDockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDockDoorClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDockDoorClaimStatus): Boolean {
        when (status) {
            WeightedDockDoorClaimStatus.IDLE -> return next == WeightedDockDoorClaimStatus.WARMING
            WeightedDockDoorClaimStatus.WARMING -> {
                when (next) {
                    WeightedDockDoorClaimStatus.READY -> return true
                }
                return false
            }
            WeightedDockDoorClaimStatus.READY -> {
                when (next) {
                    WeightedDockDoorClaimStatus.DRAINING -> return true
                }
                return false
            }
            WeightedDockDoorClaimStatus.DRAINING -> {
                when (next) {
                    WeightedDockDoorClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted dock door claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(maximum * quantity.toDouble() * 100) / 100

    fun snapshot(): WeightedDockDoorClaim {
        return WeightedDockDoorClaim(
            code = code,
            label = label,
            maximum = maximum,
            quantity = quantity,
            checksum = checksum,
            carrier = carrier,
            archived = archived,
            minimum = minimum,
            shiftCode = shiftCode,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted dock door claim in status idle. */
fun newWeightedDockDoorClaim(code: String, label: String): WeightedDockDoorClaim {
    return WeightedDockDoorClaim(
        code = code,
        label = label,
        maximum = 0.0,
        quantity = 0,
        checksum = "",
        carrier = "",
        archived = false,
        minimum = 0.0,
        shiftCode = "",
        updatedAt = 0,
        status = WeightedDockDoorClaimStatus.IDLE,
    )
}
