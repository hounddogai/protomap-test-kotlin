package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.text.normalize

/** Where a scorecard quota is in its life cycle. */
enum class ScorecardQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scorecard quota as the scorecard team keeps it. */
data class ScorecardQuota(
    var code: String,
    var label: String,
    var title: String,
    var expiresAt: Int,
    var summary: String,
    var status: ScorecardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardQuotaStatus): Boolean {
        when (status) {
            ScorecardQuotaStatus.PROPOSED -> {
                when (next) {
                    ScorecardQuotaStatus.APPROVED, ScorecardQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardQuotaStatus.APPROVED -> {
                when (next) {
                    ScorecardQuotaStatus.REJECTED, ScorecardQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardQuotaStatus.REJECTED -> {
                when (next) {
                    ScorecardQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scorecard quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): ScorecardQuota {
        return ScorecardQuota(
            code = code,
            label = label,
            title = title,
            expiresAt = expiresAt,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard quota in status proposed. */
fun newScorecardQuota(code: String, label: String): ScorecardQuota {
    return ScorecardQuota(
        code = code,
        label = label,
        title = "",
        expiresAt = 0,
        summary = "",
        status = ScorecardQuotaStatus.PROPOSED,
    )
}
