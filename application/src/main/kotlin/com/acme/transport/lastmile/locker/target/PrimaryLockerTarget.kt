package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary locker target records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a primary locker target is in its life cycle. */
enum class PrimaryLockerTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary locker target. Its code is unique within a tenant. */
data class PrimaryLockerTarget(
    var code: String,
    var label: String,
    var rate: Double,
    var carrier: String,
    var threshold: Double,
    var currency: String,
    var title: String,
    var score: Double,
    var status: PrimaryLockerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLockerTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLockerTargetStatus): Boolean {
        when (status) {
            PrimaryLockerTargetStatus.IDLE -> {
                when (next) {
                    PrimaryLockerTargetStatus.WARMING, PrimaryLockerTargetStatus.READY -> return true
                }
                return false
            }
            PrimaryLockerTargetStatus.WARMING -> return next == PrimaryLockerTargetStatus.READY || next == PrimaryLockerTargetStatus.DRAINING
            PrimaryLockerTargetStatus.READY -> {
                when (next) {
                    PrimaryLockerTargetStatus.DRAINING, PrimaryLockerTargetStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryLockerTargetStatus.DRAINING -> {
                when (next) {
                    PrimaryLockerTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLockerTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary locker target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary locker target"} $code"

    fun weightedValue(): Double = round((threshold - rate) * 100) / 100

    fun snapshot(): PrimaryLockerTarget {
        return PrimaryLockerTarget(
            code = code,
            label = label,
            rate = rate,
            carrier = carrier,
            threshold = threshold,
            currency = currency,
            title = title,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary locker target in status idle. */
fun newPrimaryLockerTarget(code: String, label: String): PrimaryLockerTarget {
    return PrimaryLockerTarget(
        code = code,
        label = label,
        rate = 0.0,
        carrier = "",
        threshold = 0.0,
        currency = "",
        title = "",
        score = 0.0,
        status = PrimaryLockerTargetStatus.IDLE,
    )
}
