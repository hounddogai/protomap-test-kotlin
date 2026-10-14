package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global emission factor proposal records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a global emission factor proposal is in its life cycle. */
enum class GlobalEmissionFactorProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global emission factor proposal. Its code is unique within a tenant. */
data class GlobalEmissionFactorProposal(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var rate: Double,
    var ratio: Double,
    var status: GlobalEmissionFactorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEmissionFactorProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEmissionFactorProposalStatus): Boolean {
        when (status) {
            GlobalEmissionFactorProposalStatus.PROPOSED -> {
                when (next) {
                    GlobalEmissionFactorProposalStatus.REJECTED, GlobalEmissionFactorProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalEmissionFactorProposalStatus.APPROVED -> return next == GlobalEmissionFactorProposalStatus.REJECTED || next == GlobalEmissionFactorProposalStatus.WITHDRAWN
            GlobalEmissionFactorProposalStatus.REJECTED -> return next == GlobalEmissionFactorProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalEmissionFactorProposalStatus) {
        if (!canMoveTo(next)) {
            error("global emission factor proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global emission factor proposal"} $code"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * rate * 100) / 100

    fun snapshot(): GlobalEmissionFactorProposal {
        return GlobalEmissionFactorProposal(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            rate = rate,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global emission factor proposal in status proposed. */
fun newGlobalEmissionFactorProposal(code: String, title: String): GlobalEmissionFactorProposal {
    return GlobalEmissionFactorProposal(
        code = code,
        title = title,
        durationMinutes = 0,
        rate = 0.0,
        ratio = 0.0,
        status = GlobalEmissionFactorProposalStatus.PROPOSED,
    )
}
