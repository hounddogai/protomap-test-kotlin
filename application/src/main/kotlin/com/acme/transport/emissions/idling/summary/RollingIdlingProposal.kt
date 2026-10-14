package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling idling proposal is in its life cycle. */
enum class RollingIdlingProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling idling proposal. Its code is unique within a tenant. */
data class RollingIdlingProposal(
    var code: String,
    var name: String,
    var threshold: Double,
    var odometer: Int,
    var vehicleId: String,
    var amount: Double,
    var status: RollingIdlingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIdlingProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIdlingProposalStatus): Boolean {
        when (status) {
            RollingIdlingProposalStatus.CREATED -> return next == RollingIdlingProposalStatus.VALIDATED
            RollingIdlingProposalStatus.VALIDATED -> return next == RollingIdlingProposalStatus.RETIRED
            RollingIdlingProposalStatus.PUBLISHED -> {
                when (next) {
                    RollingIdlingProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingIdlingProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling idling proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling idling proposal"} $code"

    fun netLoad(): Double = round((odometer.toDouble() - threshold) * 100) / 100

    fun snapshot(): RollingIdlingProposal {
        return RollingIdlingProposal(
            code = code,
            name = name,
            threshold = threshold,
            odometer = odometer,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling idling proposal in status created. */
fun newRollingIdlingProposal(code: String, name: String): RollingIdlingProposal {
    return RollingIdlingProposal(
        code = code,
        name = name,
        threshold = 0.0,
        odometer = 0,
        vehicleId = "",
        amount = 0.0,
        status = RollingIdlingProposalStatus.CREATED,
    )
}
