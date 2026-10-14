package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.text.normalize

// The global locker batch records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a global locker batch is in its life cycle. */
enum class GlobalLockerBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global locker batch, with what the planners need to act on it. */
data class GlobalLockerBatch(
    var code: String,
    var name: String,
    var locale: String,
    var retries: Int,
    var trailerId: String,
    var lane: String,
    var status: GlobalLockerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLockerBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLockerBatchStatus): Boolean {
        when (status) {
            GlobalLockerBatchStatus.CREATED -> return next == GlobalLockerBatchStatus.PUBLISHED || next == GlobalLockerBatchStatus.RETIRED
            GlobalLockerBatchStatus.VALIDATED -> {
                when (next) {
                    GlobalLockerBatchStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalLockerBatchStatus.PUBLISHED -> return next == GlobalLockerBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalLockerBatchStatus) {
        if (!canMoveTo(next)) {
            error("global locker batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global locker batch"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): GlobalLockerBatch {
        return GlobalLockerBatch(
            code = code,
            name = name,
            locale = locale,
            retries = retries,
            trailerId = trailerId,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global locker batch in status created. */
fun newGlobalLockerBatch(code: String, name: String): GlobalLockerBatch {
    return GlobalLockerBatch(
        code = code,
        name = name,
        locale = "",
        retries = 0,
        trailerId = "",
        lane = "",
        status = GlobalLockerBatchStatus.CREATED,
    )
}
