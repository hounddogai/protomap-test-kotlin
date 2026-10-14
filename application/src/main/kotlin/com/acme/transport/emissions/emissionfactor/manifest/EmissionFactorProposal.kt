package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.normalize

/** Where a emission factor proposal is in its life cycle. */
enum class EmissionFactorProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One emission factor proposal, with what the planners need to act on it. */
data class EmissionFactorProposal(
    var code: String,
    var title: String,
    var timezone: String,
    var tags: MutableList<String>,
    var segment: String,
    var active: Boolean,
    var country: String,
    var rate: Double,
    var pinned: Boolean,
    var status: EmissionFactorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorProposalStatus): Boolean {
        when (status) {
            EmissionFactorProposalStatus.OPEN -> return next == EmissionFactorProposalStatus.RESOLVED || next == EmissionFactorProposalStatus.ARCHIVED
            EmissionFactorProposalStatus.IN_REVIEW -> {
                when (next) {
                    EmissionFactorProposalStatus.RESOLVED, EmissionFactorProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorProposalStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorProposalStatus.REOPENED -> return true
                }
                return false
            }
            EmissionFactorProposalStatus.REOPENED -> return next == EmissionFactorProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EmissionFactorProposalStatus) {
        if (!canMoveTo(next)) {
            error("emission factor proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = rate

    fun snapshot(): EmissionFactorProposal {
        return EmissionFactorProposal(
            code = code,
            title = title,
            timezone = timezone,
            tags = tags,
            segment = segment,
            active = active,
            country = country,
            rate = rate,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a emission factor proposal in status open. */
fun newEmissionFactorProposal(code: String, title: String): EmissionFactorProposal {
    return EmissionFactorProposal(
        code = code,
        title = title,
        timezone = "",
        tags = mutableListOf<String>(),
        segment = "",
        active = false,
        country = "",
        rate = 0.0,
        pinned = false,
        status = EmissionFactorProposalStatus.OPEN,
    )
}
