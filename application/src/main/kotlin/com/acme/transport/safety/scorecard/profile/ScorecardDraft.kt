package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard draft is in its life cycle. */
enum class ScorecardDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scorecard draft, with what the planners need to act on it. */
data class ScorecardDraft(
    var code: String,
    var name: String,
    var timezone: String,
    var fuelLiters: Double,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var margin: Double,
    var status: ScorecardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardDraftStatus): Boolean {
        when (status) {
            ScorecardDraftStatus.NEW -> return next == ScorecardDraftStatus.RUNNING || next == ScorecardDraftStatus.FAILED
            ScorecardDraftStatus.QUEUED -> return next == ScorecardDraftStatus.FAILED
            ScorecardDraftStatus.RUNNING -> {
                when (next) {
                    ScorecardDraftStatus.DONE, ScorecardDraftStatus.FAILED -> return true
                }
                return false
            }
            ScorecardDraftStatus.DONE -> {
                when (next) {
                    ScorecardDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardDraftStatus) {
        if (!canMoveTo(next)) {
            error("scorecard draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((retries.toDouble() + margin) * 100) / 100

    fun snapshot(): ScorecardDraft {
        return ScorecardDraft(
            code = code,
            name = name,
            timezone = timezone,
            fuelLiters = fuelLiters,
            retries = retries,
            attributes = attributes,
            channel = channel,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scorecard draft in status new. */
fun newScorecardDraft(code: String, name: String): ScorecardDraft {
    return ScorecardDraft(
        code = code,
        name = name,
        timezone = "",
        fuelLiters = 0.0,
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        margin = 0.0,
        status = ScorecardDraftStatus.NEW,
    )
}
