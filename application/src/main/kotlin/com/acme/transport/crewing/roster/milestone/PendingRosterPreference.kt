package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending roster preference is in its life cycle. */
enum class PendingRosterPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending roster preference as the roster team keeps it. */
data class PendingRosterPreference(
    var code: String,
    var title: String,
    var priority: Int,
    var dock: String,
    var ratio: Double,
    var approved: Boolean,
    var phone: String,
    var status: PendingRosterPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterPreferenceStatus): Boolean {
        when (status) {
            PendingRosterPreferenceStatus.PROPOSED -> return next == PendingRosterPreferenceStatus.REJECTED || next == PendingRosterPreferenceStatus.WITHDRAWN
            PendingRosterPreferenceStatus.APPROVED -> {
                when (next) {
                    PendingRosterPreferenceStatus.REJECTED, PendingRosterPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingRosterPreferenceStatus.REJECTED -> return next == PendingRosterPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingRosterPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending roster preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round(priority.toDouble() * ratio * 100) / 100

    fun snapshot(): PendingRosterPreference {
        return PendingRosterPreference(
            code = code,
            title = title,
            priority = priority,
            dock = dock,
            ratio = ratio,
            approved = approved,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending roster preference in status proposed. */
fun newPendingRosterPreference(code: String, title: String): PendingRosterPreference {
    return PendingRosterPreference(
        code = code,
        title = title,
        priority = 0,
        dock = "",
        ratio = 0.0,
        approved = false,
        phone = "",
        status = PendingRosterPreferenceStatus.PROPOSED,
    )
}
