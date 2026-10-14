package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard digest is in its life cycle. */
enum class ScorecardDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scorecard digest and its current status. */
data class ScorecardDigest(
    var code: String,
    var label: String,
    var rate: Double,
    var etaMinutes: Int,
    var name: String,
    var sequence: Int,
    var active: Boolean,
    var status: ScorecardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardDigestStatus): Boolean {
        when (status) {
            ScorecardDigestStatus.REQUESTED -> return next == ScorecardDigestStatus.GRANTED || next == ScorecardDigestStatus.REVOKED
            ScorecardDigestStatus.GRANTED -> {
                when (next) {
                    ScorecardDigestStatus.REVOKED, ScorecardDigestStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardDigestStatus.REVOKED -> return next == ScorecardDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardDigestStatus) {
        if (!canMoveTo(next)) {
            error("scorecard digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round(etaMinutes.toDouble() * rate * 100) / 100

    fun snapshot(): ScorecardDigest {
        return ScorecardDigest(
            code = code,
            label = label,
            rate = rate,
            etaMinutes = etaMinutes,
            name = name,
            sequence = sequence,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard digest in status requested. */
fun newScorecardDigest(code: String, label: String): ScorecardDigest {
    return ScorecardDigest(
        code = code,
        label = label,
        rate = 0.0,
        etaMinutes = 0,
        name = "",
        sequence = 0,
        active = false,
        status = ScorecardDigestStatus.REQUESTED,
    )
}
