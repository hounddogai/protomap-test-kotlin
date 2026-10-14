package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.text.normalize

// The idling window records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling window is in its life cycle. */
enum class IdlingWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A idling window. Its code is unique within a tenant. */
data class IdlingWindow(
    var code: String,
    var title: String,
    var revision: Int,
    var country: String,
    var carrier: String,
    var trailerId: String,
    var status: IdlingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingWindowStatus): Boolean {
        when (status) {
            IdlingWindowStatus.DRAFT -> {
                when (next) {
                    IdlingWindowStatus.PENDING, IdlingWindowStatus.ACTIVE -> return true
                }
                return false
            }
            IdlingWindowStatus.PENDING -> {
                when (next) {
                    IdlingWindowStatus.ACTIVE, IdlingWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            IdlingWindowStatus.ACTIVE -> {
                when (next) {
                    IdlingWindowStatus.SUSPENDED, IdlingWindowStatus.CLOSED -> return true
                }
                return false
            }
            IdlingWindowStatus.SUSPENDED -> return next == IdlingWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IdlingWindowStatus) {
        if (!canMoveTo(next)) {
            error("idling window $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): IdlingWindow {
        return IdlingWindow(
            code = code,
            title = title,
            revision = revision,
            country = country,
            carrier = carrier,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a idling window in status draft. */
fun newIdlingWindow(code: String, title: String): IdlingWindow {
    return IdlingWindow(
        code = code,
        title = title,
        revision = 0,
        country = "",
        carrier = "",
        trailerId = "",
        status = IdlingWindowStatus.DRAFT,
    )
}
