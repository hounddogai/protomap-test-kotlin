package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual cutoff draft is in its life cycle. */
enum class ManualCutoffDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual cutoff draft and its current status. */
data class ManualCutoffDraft(
    var code: String,
    var label: String,
    var country: String,
    var depot: String,
    var approved: Boolean,
    var rate: Double,
    var carrier: String,
    var score: Double,
    var distanceKm: Double,
    var minimum: Double,
    var channel: String,
    var status: ManualCutoffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCutoffDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCutoffDraftStatus): Boolean {
        when (status) {
            ManualCutoffDraftStatus.IDLE -> return next == ManualCutoffDraftStatus.STOPPED
            ManualCutoffDraftStatus.WARMING -> {
                when (next) {
                    ManualCutoffDraftStatus.DRAINING -> return true
                }
                return false
            }
            ManualCutoffDraftStatus.READY -> {
                when (next) {
                    ManualCutoffDraftStatus.DRAINING, ManualCutoffDraftStatus.STOPPED -> return true
                }
                return false
            }
            ManualCutoffDraftStatus.DRAINING -> return next == ManualCutoffDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualCutoffDraftStatus) {
        if (!canMoveTo(next)) {
            error("manual cutoff draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(score * minimum * 100) / 100

    fun snapshot(): ManualCutoffDraft {
        return ManualCutoffDraft(
            code = code,
            label = label,
            country = country,
            depot = depot,
            approved = approved,
            rate = rate,
            carrier = carrier,
            score = score,
            distanceKm = distanceKm,
            minimum = minimum,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual cutoff draft in status idle. */
fun newManualCutoffDraft(code: String, label: String): ManualCutoffDraft {
    return ManualCutoffDraft(
        code = code,
        label = label,
        country = "",
        depot = "",
        approved = false,
        rate = 0.0,
        carrier = "",
        score = 0.0,
        distanceKm = 0.0,
        minimum = 0.0,
        channel = "",
        status = ManualCutoffDraftStatus.IDLE,
    )
}
