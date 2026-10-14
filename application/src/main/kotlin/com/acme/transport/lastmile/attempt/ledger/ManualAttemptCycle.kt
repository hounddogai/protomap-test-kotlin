package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual attempt cycle is in its life cycle. */
enum class ManualAttemptCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual attempt cycle. Its code is unique within a tenant. */
data class ManualAttemptCycle(
    var code: String,
    var name: String,
    var sequence: Int,
    var updatedAt: Int,
    var pallets: Int,
    var description: String,
    var score: Double,
    var durationMinutes: Int,
    var status: ManualAttemptCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptCycleStatus): Boolean {
        when (status) {
            ManualAttemptCycleStatus.DRAFT -> return next == ManualAttemptCycleStatus.PENDING || next == ManualAttemptCycleStatus.SUSPENDED
            ManualAttemptCycleStatus.PENDING -> {
                when (next) {
                    ManualAttemptCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualAttemptCycleStatus.ACTIVE -> {
                when (next) {
                    ManualAttemptCycleStatus.SUSPENDED, ManualAttemptCycleStatus.CLOSED -> return true
                }
                return false
            }
            ManualAttemptCycleStatus.SUSPENDED -> {
                when (next) {
                    ManualAttemptCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAttemptCycleStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(sequence.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): ManualAttemptCycle {
        return ManualAttemptCycle(
            code = code,
            name = name,
            sequence = sequence,
            updatedAt = updatedAt,
            pallets = pallets,
            description = description,
            score = score,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual attempt cycle in status draft. */
fun newManualAttemptCycle(code: String, name: String): ManualAttemptCycle {
    return ManualAttemptCycle(
        code = code,
        name = name,
        sequence = 0,
        updatedAt = 0,
        pallets = 0,
        description = "",
        score = 0.0,
        durationMinutes = 0,
        status = ManualAttemptCycleStatus.DRAFT,
    )
}
