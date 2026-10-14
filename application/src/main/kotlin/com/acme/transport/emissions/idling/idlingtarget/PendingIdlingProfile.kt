package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize

/** Where a pending idling profile is in its life cycle. */
enum class PendingIdlingProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending idling profile, with what the planners need to act on it. */
data class PendingIdlingProfile(
    var code: String,
    var name: String,
    var description: String,
    var trailerId: String,
    var currency: String,
    var amount: Double,
    var status: PendingIdlingProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingProfileStatus): Boolean {
        when (status) {
            PendingIdlingProfileStatus.NEW -> {
                when (next) {
                    PendingIdlingProfileStatus.DONE -> return true
                }
                return false
            }
            PendingIdlingProfileStatus.QUEUED -> {
                when (next) {
                    PendingIdlingProfileStatus.DONE, PendingIdlingProfileStatus.FAILED -> return true
                }
                return false
            }
            PendingIdlingProfileStatus.RUNNING -> return next == PendingIdlingProfileStatus.DONE
            PendingIdlingProfileStatus.DONE -> {
                when (next) {
                    PendingIdlingProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingIdlingProfileStatus) {
        if (!canMoveTo(next)) {
            error("pending idling profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending idling profile"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): PendingIdlingProfile {
        return PendingIdlingProfile(
            code = code,
            name = name,
            description = description,
            trailerId = trailerId,
            currency = currency,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending idling profile in status new. */
fun newPendingIdlingProfile(code: String, name: String): PendingIdlingProfile {
    return PendingIdlingProfile(
        code = code,
        name = name,
        description = "",
        trailerId = "",
        currency = "",
        amount = 0.0,
        status = PendingIdlingProfileStatus.NEW,
    )
}
