package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal parcel proposal is in its life cycle. */
enum class SeasonalParcelProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal parcel proposal, with what the planners need to act on it. */
data class SeasonalParcelProposal(
    var code: String,
    var name: String,
    var endsAt: Int,
    var notes: String,
    var revision: Int,
    var graceDays: Int,
    var currency: String,
    var amount: Double,
    var windowDays: Int,
    var driverId: String,
    var status: SeasonalParcelProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalParcelProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalParcelProposalStatus): Boolean {
        when (status) {
            SeasonalParcelProposalStatus.PLANNED -> {
                when (next) {
                    SeasonalParcelProposalStatus.IN_PROGRESS, SeasonalParcelProposalStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalParcelProposalStatus.SCHEDULED -> {
                when (next) {
                    SeasonalParcelProposalStatus.COMPLETED, SeasonalParcelProposalStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalParcelProposalStatus.IN_PROGRESS -> return next == SeasonalParcelProposalStatus.CANCELLED
            SeasonalParcelProposalStatus.COMPLETED -> return next == SeasonalParcelProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalParcelProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal parcel proposal $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"seasonal parcel proposal"} $code"

    fun effectiveValue(): Double = round(endsAt.toDouble() * amount * 100) / 100

    fun snapshot(): SeasonalParcelProposal {
        return SeasonalParcelProposal(
            code = code,
            name = name,
            endsAt = endsAt,
            notes = notes,
            revision = revision,
            graceDays = graceDays,
            currency = currency,
            amount = amount,
            windowDays = windowDays,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal parcel proposal in status planned. */
fun newSeasonalParcelProposal(code: String, name: String): SeasonalParcelProposal {
    return SeasonalParcelProposal(
        code = code,
        name = name,
        endsAt = 0,
        notes = "",
        revision = 0,
        graceDays = 0,
        currency = "",
        amount = 0.0,
        windowDays = 0,
        driverId = "",
        status = SeasonalParcelProposalStatus.PLANNED,
    )
}
