package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize

/** Where a primary tire draft is in its life cycle. */
enum class PrimaryTireDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary tire draft as the tire team keeps it. */
data class PrimaryTireDraft(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var category: String,
    var title: String,
    var status: PrimaryTireDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireDraftStatus): Boolean {
        when (status) {
            PrimaryTireDraftStatus.PROPOSED -> {
                when (next) {
                    PrimaryTireDraftStatus.APPROVED, PrimaryTireDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTireDraftStatus.APPROVED -> {
                when (next) {
                    PrimaryTireDraftStatus.REJECTED, PrimaryTireDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTireDraftStatus.REJECTED -> {
                when (next) {
                    PrimaryTireDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTireDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary tire draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): PrimaryTireDraft {
        return PrimaryTireDraft(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            category = category,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary tire draft in status proposed. */
fun newPrimaryTireDraft(code: String, label: String): PrimaryTireDraft {
    return PrimaryTireDraft(
        code = code,
        label = label,
        etaMinutes = 0,
        category = "",
        title = "",
        status = PrimaryTireDraftStatus.PROPOSED,
    )
}
