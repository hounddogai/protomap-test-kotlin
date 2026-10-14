package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled cutoff draft is in its life cycle. */
enum class ScheduledCutoffDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled cutoff draft. Its code is unique within a tenant. */
data class ScheduledCutoffDraft(
    var code: String,
    var label: String,
    var maximum: Double,
    var depot: String,
    var attempts: Int,
    var country: String,
    var status: ScheduledCutoffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCutoffDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCutoffDraftStatus): Boolean {
        when (status) {
            ScheduledCutoffDraftStatus.DRAFT -> {
                when (next) {
                    ScheduledCutoffDraftStatus.PENDING, ScheduledCutoffDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledCutoffDraftStatus.PENDING -> return next == ScheduledCutoffDraftStatus.SUSPENDED || next == ScheduledCutoffDraftStatus.CLOSED
            ScheduledCutoffDraftStatus.ACTIVE -> {
                when (next) {
                    ScheduledCutoffDraftStatus.SUSPENDED, ScheduledCutoffDraftStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledCutoffDraftStatus.SUSPENDED -> {
                when (next) {
                    ScheduledCutoffDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCutoffDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cutoff draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled cutoff draft"} $code"

    fun adjustedLoad(): Double = round(maximum * attempts.toDouble() * 100) / 100

    fun snapshot(): ScheduledCutoffDraft {
        return ScheduledCutoffDraft(
            code = code,
            label = label,
            maximum = maximum,
            depot = depot,
            attempts = attempts,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled cutoff draft in status draft. */
fun newScheduledCutoffDraft(code: String, label: String): ScheduledCutoffDraft {
    return ScheduledCutoffDraft(
        code = code,
        label = label,
        maximum = 0.0,
        depot = "",
        attempts = 0,
        country = "",
        status = ScheduledCutoffDraftStatus.DRAFT,
    )
}
