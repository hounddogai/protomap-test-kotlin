package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient relay score is in its life cycle. */
enum class LenientRelayScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient relay score as the relay team keeps it. */
data class LenientRelayScore(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var minimum: Double,
    var contactEmail: String,
    var budget: Double,
    var durationMinutes: Int,
    var dock: String,
    var status: LenientRelayScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayScoreStatus): Boolean {
        when (status) {
            LenientRelayScoreStatus.NEW -> {
                when (next) {
                    LenientRelayScoreStatus.QUEUED, LenientRelayScoreStatus.DONE -> return true
                }
                return false
            }
            LenientRelayScoreStatus.QUEUED -> {
                when (next) {
                    LenientRelayScoreStatus.RUNNING, LenientRelayScoreStatus.FAILED -> return true
                }
                return false
            }
            LenientRelayScoreStatus.RUNNING -> return next == LenientRelayScoreStatus.DONE
            LenientRelayScoreStatus.DONE -> return next == LenientRelayScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientRelayScoreStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((durationMinutes.toDouble() - budget) * 100) / 100

    fun snapshot(): LenientRelayScore {
        return LenientRelayScore(
            code = code,
            label = label,
            pinned = pinned,
            minimum = minimum,
            contactEmail = contactEmail,
            budget = budget,
            durationMinutes = durationMinutes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient relay score in status new. */
fun newLenientRelayScore(code: String, label: String): LenientRelayScore {
    return LenientRelayScore(
        code = code,
        label = label,
        pinned = false,
        minimum = 0.0,
        contactEmail = "",
        budget = 0.0,
        durationMinutes = 0,
        dock = "",
        status = LenientRelayScoreStatus.NEW,
    )
}
