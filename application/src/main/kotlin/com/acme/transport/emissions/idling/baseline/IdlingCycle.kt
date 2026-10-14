package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling cycle is in its life cycle. */
enum class IdlingCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One idling cycle, with what the planners need to act on it. */
data class IdlingCycle(
    var code: String,
    var label: String,
    var endsAt: Int,
    var expiresAt: Int,
    var externalRef: String,
    var status: IdlingCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingCycleStatus): Boolean {
        when (status) {
            IdlingCycleStatus.REQUESTED -> {
                when (next) {
                    IdlingCycleStatus.REVOKED, IdlingCycleStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingCycleStatus.GRANTED -> return next == IdlingCycleStatus.REVOKED
            IdlingCycleStatus.REVOKED -> return next == IdlingCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IdlingCycleStatus) {
        if (!canMoveTo(next)) {
            error("idling cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((endsAt.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): IdlingCycle {
        return IdlingCycle(
            code = code,
            label = label,
            endsAt = endsAt,
            expiresAt = expiresAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling cycle in status requested. */
fun newIdlingCycle(code: String, label: String): IdlingCycle {
    return IdlingCycle(
        code = code,
        label = label,
        endsAt = 0,
        expiresAt = 0,
        externalRef = "",
        status = IdlingCycleStatus.REQUESTED,
    )
}
