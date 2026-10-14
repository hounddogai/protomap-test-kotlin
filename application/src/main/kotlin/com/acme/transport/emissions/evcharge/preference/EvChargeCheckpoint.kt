package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge checkpoint is in its life cycle. */
enum class EvChargeCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One ev charge checkpoint, with what the planners need to act on it. */
data class EvChargeCheckpoint(
    var code: String,
    var title: String,
    var quantity: Int,
    var pallets: Int,
    var label: String,
    var carrier: String,
    var status: EvChargeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeCheckpointStatus): Boolean {
        when (status) {
            EvChargeCheckpointStatus.DRAFT -> return next == EvChargeCheckpointStatus.ACTIVE || next == EvChargeCheckpointStatus.CLOSED
            EvChargeCheckpointStatus.PENDING -> return next == EvChargeCheckpointStatus.ACTIVE
            EvChargeCheckpointStatus.ACTIVE -> {
                when (next) {
                    EvChargeCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            EvChargeCheckpointStatus.SUSPENDED -> return next == EvChargeCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: EvChargeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("ev charge checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedTotal(): Double = round((quantity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): EvChargeCheckpoint {
        return EvChargeCheckpoint(
            code = code,
            title = title,
            quantity = quantity,
            pallets = pallets,
            label = label,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a ev charge checkpoint in status draft. */
fun newEvChargeCheckpoint(code: String, title: String): EvChargeCheckpoint {
    return EvChargeCheckpoint(
        code = code,
        title = title,
        quantity = 0,
        pallets = 0,
        label = "",
        carrier = "",
        status = EvChargeCheckpointStatus.DRAFT,
    )
}
