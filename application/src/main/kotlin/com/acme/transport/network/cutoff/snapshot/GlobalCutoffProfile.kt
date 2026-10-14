package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize

/** Where a global cutoff profile is in its life cycle. */
enum class GlobalCutoffProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global cutoff profile as the cutoff team keeps it. */
data class GlobalCutoffProfile(
    var code: String,
    var title: String,
    var ownerId: String,
    var unit: String,
    var summary: String,
    var country: String,
    var ceiling: Double,
    var status: GlobalCutoffProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffProfileStatus): Boolean {
        when (status) {
            GlobalCutoffProfileStatus.PROPOSED -> return next == GlobalCutoffProfileStatus.APPROVED || next == GlobalCutoffProfileStatus.REJECTED
            GlobalCutoffProfileStatus.APPROVED -> return next == GlobalCutoffProfileStatus.REJECTED || next == GlobalCutoffProfileStatus.WITHDRAWN
            GlobalCutoffProfileStatus.REJECTED -> {
                when (next) {
                    GlobalCutoffProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffProfileStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = ceiling

    fun snapshot(): GlobalCutoffProfile {
        return GlobalCutoffProfile(
            code = code,
            title = title,
            ownerId = ownerId,
            unit = unit,
            summary = summary,
            country = country,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global cutoff profile in status proposed. */
fun newGlobalCutoffProfile(code: String, title: String): GlobalCutoffProfile {
    return GlobalCutoffProfile(
        code = code,
        title = title,
        ownerId = "",
        unit = "",
        summary = "",
        country = "",
        ceiling = 0.0,
        status = GlobalCutoffProfileStatus.PROPOSED,
    )
}
