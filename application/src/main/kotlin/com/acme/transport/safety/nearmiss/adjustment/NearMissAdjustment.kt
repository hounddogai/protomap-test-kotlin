package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.text.normalize

// The near miss adjustment records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss adjustment is in its life cycle. */
enum class NearMissAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A near miss adjustment as the near miss team keeps it. */
data class NearMissAdjustment(
    var code: String,
    var title: String,
    var amount: Double,
    var archived: Boolean,
    var shiftCode: String,
    var status: NearMissAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissAdjustmentStatus): Boolean {
        when (status) {
            NearMissAdjustmentStatus.NEW -> return next == NearMissAdjustmentStatus.RUNNING || next == NearMissAdjustmentStatus.FAILED
            NearMissAdjustmentStatus.QUEUED -> {
                when (next) {
                    NearMissAdjustmentStatus.RUNNING, NearMissAdjustmentStatus.DONE -> return true
                }
                return false
            }
            NearMissAdjustmentStatus.RUNNING -> return next == NearMissAdjustmentStatus.DONE || next == NearMissAdjustmentStatus.FAILED
            NearMissAdjustmentStatus.DONE -> {
                when (next) {
                    NearMissAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("near miss adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): NearMissAdjustment {
        return NearMissAdjustment(
            code = code,
            title = title,
            amount = amount,
            archived = archived,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss adjustment in status new. */
fun newNearMissAdjustment(code: String, title: String): NearMissAdjustment {
    return NearMissAdjustment(
        code = code,
        title = title,
        amount = 0.0,
        archived = false,
        shiftCode = "",
        status = NearMissAdjustmentStatus.NEW,
    )
}
