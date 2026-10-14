package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary trailer yard cycle records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a primary trailer yard cycle is in its life cycle. */
enum class PrimaryTrailerYardCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary trailer yard cycle. Its code is unique within a tenant. */
data class PrimaryTrailerYardCycle(
    var code: String,
    var label: String,
    var endsAt: Int,
    var amount: Double,
    var pinned: Boolean,
    var score: Double,
    var segment: String,
    var expiresAt: Int,
    var archived: Boolean,
    var status: PrimaryTrailerYardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerYardCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerYardCycleStatus): Boolean {
        when (status) {
            PrimaryTrailerYardCycleStatus.PROPOSED -> {
                when (next) {
                    PrimaryTrailerYardCycleStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryTrailerYardCycleStatus.APPROVED -> {
                when (next) {
                    PrimaryTrailerYardCycleStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryTrailerYardCycleStatus.REJECTED -> return next == PrimaryTrailerYardCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerYardCycleStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer yard cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((endsAt.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTrailerYardCycle {
        return PrimaryTrailerYardCycle(
            code = code,
            label = label,
            endsAt = endsAt,
            amount = amount,
            pinned = pinned,
            score = score,
            segment = segment,
            expiresAt = expiresAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary trailer yard cycle in status proposed. */
fun newPrimaryTrailerYardCycle(code: String, label: String): PrimaryTrailerYardCycle {
    return PrimaryTrailerYardCycle(
        code = code,
        label = label,
        endsAt = 0,
        amount = 0.0,
        pinned = false,
        score = 0.0,
        segment = "",
        expiresAt = 0,
        archived = false,
        status = PrimaryTrailerYardCycleStatus.PROPOSED,
    )
}
