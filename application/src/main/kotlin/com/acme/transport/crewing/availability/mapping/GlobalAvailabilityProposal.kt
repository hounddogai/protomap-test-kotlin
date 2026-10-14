package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global availability proposal is in its life cycle. */
enum class GlobalAvailabilityProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global availability proposal as the availability team keeps it. */
data class GlobalAvailabilityProposal(
    var code: String,
    var label: String,
    var windowDays: Int,
    var retries: Int,
    var graceDays: Int,
    var budget: Double,
    var floorValue: Double,
    var attempts: Int,
    var channel: String,
    var status: GlobalAvailabilityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAvailabilityProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAvailabilityProposalStatus): Boolean {
        when (status) {
            GlobalAvailabilityProposalStatus.PLANNED -> {
                when (next) {
                    GlobalAvailabilityProposalStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalAvailabilityProposalStatus.SCHEDULED -> {
                when (next) {
                    GlobalAvailabilityProposalStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalAvailabilityProposalStatus.IN_PROGRESS -> return next == GlobalAvailabilityProposalStatus.COMPLETED || next == GlobalAvailabilityProposalStatus.CANCELLED
            GlobalAvailabilityProposalStatus.COMPLETED -> {
                when (next) {
                    GlobalAvailabilityProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAvailabilityProposalStatus) {
        if (!canMoveTo(next)) {
            error("global availability proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round(attempts.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): GlobalAvailabilityProposal {
        return GlobalAvailabilityProposal(
            code = code,
            label = label,
            windowDays = windowDays,
            retries = retries,
            graceDays = graceDays,
            budget = budget,
            floorValue = floorValue,
            attempts = attempts,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global availability proposal in status planned. */
fun newGlobalAvailabilityProposal(code: String, label: String): GlobalAvailabilityProposal {
    return GlobalAvailabilityProposal(
        code = code,
        label = label,
        windowDays = 0,
        retries = 0,
        graceDays = 0,
        budget = 0.0,
        floorValue = 0.0,
        attempts = 0,
        channel = "",
        status = GlobalAvailabilityProposalStatus.PLANNED,
    )
}
