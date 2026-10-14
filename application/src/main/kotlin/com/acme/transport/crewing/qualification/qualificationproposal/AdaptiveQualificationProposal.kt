package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive qualification proposal records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a adaptive qualification proposal is in its life cycle. */
enum class AdaptiveQualificationProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive qualification proposal. Its code is unique within a tenant. */
data class AdaptiveQualificationProposal(
    var code: String,
    var title: String,
    var amount: Double,
    var segment: String,
    var shiftCode: String,
    var loadKg: Double,
    var status: AdaptiveQualificationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveQualificationProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveQualificationProposalStatus): Boolean {
        when (status) {
            AdaptiveQualificationProposalStatus.CREATED -> return next == AdaptiveQualificationProposalStatus.PUBLISHED
            AdaptiveQualificationProposalStatus.VALIDATED -> {
                when (next) {
                    AdaptiveQualificationProposalStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveQualificationProposalStatus.PUBLISHED -> return next == AdaptiveQualificationProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveQualificationProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive qualification proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((loadKg - amount) * 100) / 100

    fun snapshot(): AdaptiveQualificationProposal {
        return AdaptiveQualificationProposal(
            code = code,
            title = title,
            amount = amount,
            segment = segment,
            shiftCode = shiftCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive qualification proposal in status created. */
fun newAdaptiveQualificationProposal(code: String, title: String): AdaptiveQualificationProposal {
    return AdaptiveQualificationProposal(
        code = code,
        title = title,
        amount = 0.0,
        segment = "",
        shiftCode = "",
        loadKg = 0.0,
        status = AdaptiveQualificationProposalStatus.CREATED,
    )
}
