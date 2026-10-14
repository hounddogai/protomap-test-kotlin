package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cutoff assignment is in its life cycle. */
enum class LenientCutoffAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient cutoff assignment and its current status. */
data class LenientCutoffAssignment(
    var code: String,
    var name: String,
    var retries: Int,
    var updatedAt: Int,
    var pinned: Boolean,
    var discount: Double,
    var notes: String,
    var cost: Double,
    var priority: Int,
    var rate: Double,
    var expiresAt: Int,
    var status: LenientCutoffAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffAssignmentStatus): Boolean {
        when (status) {
            LenientCutoffAssignmentStatus.CREATED -> return next == LenientCutoffAssignmentStatus.PUBLISHED
            LenientCutoffAssignmentStatus.VALIDATED -> {
                when (next) {
                    LenientCutoffAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientCutoffAssignmentStatus.PUBLISHED -> return next == LenientCutoffAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientCutoffAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round(retries.toDouble() * cost * 100) / 100

    fun snapshot(): LenientCutoffAssignment {
        return LenientCutoffAssignment(
            code = code,
            name = name,
            retries = retries,
            updatedAt = updatedAt,
            pinned = pinned,
            discount = discount,
            notes = notes,
            cost = cost,
            priority = priority,
            rate = rate,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient cutoff assignment in status created. */
fun newLenientCutoffAssignment(code: String, name: String): LenientCutoffAssignment {
    return LenientCutoffAssignment(
        code = code,
        name = name,
        retries = 0,
        updatedAt = 0,
        pinned = false,
        discount = 0.0,
        notes = "",
        cost = 0.0,
        priority = 0,
        rate = 0.0,
        expiresAt = 0,
        status = LenientCutoffAssignmentStatus.CREATED,
    )
}
