package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster baseline is in its life cycle. */
enum class StrictRosterBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict roster baseline. Its code is unique within a tenant. */
data class StrictRosterBaseline(
    var code: String,
    var title: String,
    var label: String,
    var priority: Int,
    var approved: Boolean,
    var sequence: Int,
    var expiresAt: Int,
    var locale: String,
    var rate: Double,
    var archived: Boolean,
    var minimum: Double,
    var status: StrictRosterBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterBaselineStatus): Boolean {
        when (status) {
            StrictRosterBaselineStatus.REQUESTED -> return next == StrictRosterBaselineStatus.REVOKED
            StrictRosterBaselineStatus.GRANTED -> {
                when (next) {
                    StrictRosterBaselineStatus.REVOKED -> return true
                }
                return false
            }
            StrictRosterBaselineStatus.REVOKED -> return next == StrictRosterBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictRosterBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict roster baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveValue(): Double = round((priority.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictRosterBaseline {
        return StrictRosterBaseline(
            code = code,
            title = title,
            label = label,
            priority = priority,
            approved = approved,
            sequence = sequence,
            expiresAt = expiresAt,
            locale = locale,
            rate = rate,
            archived = archived,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict roster baseline in status requested. */
fun newStrictRosterBaseline(code: String, title: String): StrictRosterBaseline {
    return StrictRosterBaseline(
        code = code,
        title = title,
        label = "",
        priority = 0,
        approved = false,
        sequence = 0,
        expiresAt = 0,
        locale = "",
        rate = 0.0,
        archived = false,
        minimum = 0.0,
        status = StrictRosterBaselineStatus.REQUESTED,
    )
}
