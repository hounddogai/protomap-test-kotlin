package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge proposal is in its life cycle. */
enum class EvChargeProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a ev charge proposal and its current status. */
data class EvChargeProposal(
    var code: String,
    var label: String,
    var endsAt: Int,
    var distanceKm: Double,
    var visible: Boolean,
    var threshold: Double,
    var minimum: Double,
    var attempts: Int,
    var category: String,
    var status: EvChargeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeProposalStatus): Boolean {
        when (status) {
            EvChargeProposalStatus.OPEN -> {
                when (next) {
                    EvChargeProposalStatus.IN_REVIEW -> return true
                }
                return false
            }
            EvChargeProposalStatus.IN_REVIEW -> {
                when (next) {
                    EvChargeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeProposalStatus.RESOLVED -> return next == EvChargeProposalStatus.REOPENED || next == EvChargeProposalStatus.ARCHIVED
            EvChargeProposalStatus.REOPENED -> return next == EvChargeProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EvChargeProposalStatus) {
        if (!canMoveTo(next)) {
            error("ev charge proposal $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round((distanceKm + minimum) * 100) / 100

    fun snapshot(): EvChargeProposal {
        return EvChargeProposal(
            code = code,
            label = label,
            endsAt = endsAt,
            distanceKm = distanceKm,
            visible = visible,
            threshold = threshold,
            minimum = minimum,
            attempts = attempts,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a ev charge proposal in status open. */
fun newEvChargeProposal(code: String, label: String): EvChargeProposal {
    return EvChargeProposal(
        code = code,
        label = label,
        endsAt = 0,
        distanceKm = 0.0,
        visible = false,
        threshold = 0.0,
        minimum = 0.0,
        attempts = 0,
        category = "",
        status = EvChargeProposalStatus.OPEN,
    )
}
