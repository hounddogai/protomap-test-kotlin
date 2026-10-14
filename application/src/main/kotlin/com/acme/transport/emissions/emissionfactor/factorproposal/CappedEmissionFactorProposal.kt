package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped emission factor proposal records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a capped emission factor proposal is in its life cycle. */
enum class CappedEmissionFactorProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped emission factor proposal, with what the planners need to act on it. */
data class CappedEmissionFactorProposal(
    var code: String,
    var title: String,
    var ceiling: Double,
    var approved: Boolean,
    var endsAt: Int,
    var archived: Boolean,
    var dock: String,
    var label: String,
    var driverId: String,
    var status: CappedEmissionFactorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedEmissionFactorProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedEmissionFactorProposalStatus): Boolean {
        when (status) {
            CappedEmissionFactorProposalStatus.CREATED -> {
                when (next) {
                    CappedEmissionFactorProposalStatus.VALIDATED -> return true
                }
                return false
            }
            CappedEmissionFactorProposalStatus.VALIDATED -> return next == CappedEmissionFactorProposalStatus.PUBLISHED || next == CappedEmissionFactorProposalStatus.RETIRED
            CappedEmissionFactorProposalStatus.PUBLISHED -> return next == CappedEmissionFactorProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedEmissionFactorProposalStatus) {
        if (!canMoveTo(next)) {
            error("capped emission factor proposal $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun netVolume(): Double = round(endsAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): CappedEmissionFactorProposal {
        return CappedEmissionFactorProposal(
            code = code,
            title = title,
            ceiling = ceiling,
            approved = approved,
            endsAt = endsAt,
            archived = archived,
            dock = dock,
            label = label,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped emission factor proposal in status created. */
fun newCappedEmissionFactorProposal(code: String, title: String): CappedEmissionFactorProposal {
    return CappedEmissionFactorProposal(
        code = code,
        title = title,
        ceiling = 0.0,
        approved = false,
        endsAt = 0,
        archived = false,
        dock = "",
        label = "",
        driverId = "",
        status = CappedEmissionFactorProposalStatus.CREATED,
    )
}
