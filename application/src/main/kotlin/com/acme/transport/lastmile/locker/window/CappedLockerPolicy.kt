package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped locker policy is in its life cycle. */
enum class CappedLockerPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped locker policy, with what the planners need to act on it. */
data class CappedLockerPolicy(
    var code: String,
    var name: String,
    var startsAt: Int,
    var source: String,
    var retries: Int,
    var ownerId: String,
    var lane: String,
    var status: CappedLockerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerPolicyStatus): Boolean {
        when (status) {
            CappedLockerPolicyStatus.CREATED -> {
                when (next) {
                    CappedLockerPolicyStatus.VALIDATED, CappedLockerPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedLockerPolicyStatus.VALIDATED -> return next == CappedLockerPolicyStatus.PUBLISHED || next == CappedLockerPolicyStatus.RETIRED
            CappedLockerPolicyStatus.PUBLISHED -> return next == CappedLockerPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedLockerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped locker policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round(startsAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): CappedLockerPolicy {
        return CappedLockerPolicy(
            code = code,
            name = name,
            startsAt = startsAt,
            source = source,
            retries = retries,
            ownerId = ownerId,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped locker policy in status created. */
fun newCappedLockerPolicy(code: String, name: String): CappedLockerPolicy {
    return CappedLockerPolicy(
        code = code,
        name = name,
        startsAt = 0,
        source = "",
        retries = 0,
        ownerId = "",
        lane = "",
        status = CappedLockerPolicyStatus.CREATED,
    )
}
