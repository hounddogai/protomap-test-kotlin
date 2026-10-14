package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock score is in its life cycle. */
enum class CrossDockScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A cross dock score. Its code is unique within a tenant. */
data class CrossDockScore(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var priority: Int,
    var vehicleId: String,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var carrier: String,
    var status: CrossDockScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockScoreStatus): Boolean {
        when (status) {
            CrossDockScoreStatus.PROPOSED -> return next == CrossDockScoreStatus.WITHDRAWN
            CrossDockScoreStatus.APPROVED -> return next == CrossDockScoreStatus.REJECTED
            CrossDockScoreStatus.REJECTED -> return next == CrossDockScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CrossDockScoreStatus) {
        if (!canMoveTo(next)) {
            error("cross dock score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((priority.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): CrossDockScore {
        return CrossDockScore(
            code = code,
            label = label,
            pinned = pinned,
            priority = priority,
            vehicleId = vehicleId,
            tags = tags,
            fuelLiters = fuelLiters,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock score in status proposed. */
fun newCrossDockScore(code: String, label: String): CrossDockScore {
    return CrossDockScore(
        code = code,
        label = label,
        pinned = false,
        priority = 0,
        vehicleId = "",
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        carrier = "",
        status = CrossDockScoreStatus.PROPOSED,
    )
}
