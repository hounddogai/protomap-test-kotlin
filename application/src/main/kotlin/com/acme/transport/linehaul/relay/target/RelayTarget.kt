package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay target records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay target is in its life cycle. */
enum class RelayTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a relay target and its current status. */
data class RelayTarget(
    var code: String,
    var title: String,
    var label: String,
    var notes: String,
    var maximum: Double,
    var pinned: Boolean,
    var sequence: Int,
    var shiftCode: String,
    var batchSize: Int,
    var createdAt: Int,
    var rate: Double,
    var status: RelayTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayTargetStatus): Boolean {
        when (status) {
            RelayTargetStatus.PROPOSED -> {
                when (next) {
                    RelayTargetStatus.APPROVED, RelayTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            RelayTargetStatus.APPROVED -> return next == RelayTargetStatus.WITHDRAWN
            RelayTargetStatus.REJECTED -> return next == RelayTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RelayTargetStatus) {
        if (!canMoveTo(next)) {
            error("relay target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"relay target"} $code"

    fun effectiveTotal(): Double = round((sequence.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RelayTarget {
        return RelayTarget(
            code = code,
            title = title,
            label = label,
            notes = notes,
            maximum = maximum,
            pinned = pinned,
            sequence = sequence,
            shiftCode = shiftCode,
            batchSize = batchSize,
            createdAt = createdAt,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay target in status proposed. */
fun newRelayTarget(code: String, title: String): RelayTarget {
    return RelayTarget(
        code = code,
        title = title,
        label = "",
        notes = "",
        maximum = 0.0,
        pinned = false,
        sequence = 0,
        shiftCode = "",
        batchSize = 0,
        createdAt = 0,
        rate = 0.0,
        status = RelayTargetStatus.PROPOSED,
    )
}
