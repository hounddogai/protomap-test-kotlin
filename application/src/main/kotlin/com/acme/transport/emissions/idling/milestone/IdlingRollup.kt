package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling rollup is in its life cycle. */
enum class IdlingRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a idling rollup and its current status. */
data class IdlingRollup(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var unit: String,
    var contactEmail: String,
    var depot: String,
    var capacity: Int,
    var status: IdlingRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingRollupStatus): Boolean {
        when (status) {
            IdlingRollupStatus.NEW -> return next == IdlingRollupStatus.DONE || next == IdlingRollupStatus.FAILED
            IdlingRollupStatus.QUEUED -> {
                when (next) {
                    IdlingRollupStatus.DONE, IdlingRollupStatus.FAILED -> return true
                }
                return false
            }
            IdlingRollupStatus.RUNNING -> {
                when (next) {
                    IdlingRollupStatus.DONE, IdlingRollupStatus.FAILED -> return true
                }
                return false
            }
            IdlingRollupStatus.DONE -> return next == IdlingRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IdlingRollupStatus) {
        if (!canMoveTo(next)) {
            error("idling rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((capacity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): IdlingRollup {
        return IdlingRollup(
            code = code,
            label = label,
            expiresAt = expiresAt,
            unit = unit,
            contactEmail = contactEmail,
            depot = depot,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a idling rollup in status new. */
fun newIdlingRollup(code: String, label: String): IdlingRollup {
    return IdlingRollup(
        code = code,
        label = label,
        expiresAt = 0,
        unit = "",
        contactEmail = "",
        depot = "",
        capacity = 0,
        status = IdlingRollupStatus.NEW,
    )
}
