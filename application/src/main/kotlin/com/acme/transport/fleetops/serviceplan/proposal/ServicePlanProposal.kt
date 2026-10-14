package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan proposal records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan proposal is in its life cycle. */
enum class ServicePlanProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan proposal as the service plan team keeps it. */
data class ServicePlanProposal(
    var code: String,
    var label: String,
    var priority: Int,
    var maximum: Double,
    var locale: String,
    var score: Double,
    var status: ServicePlanProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanProposalStatus): Boolean {
        when (status) {
            ServicePlanProposalStatus.CREATED -> return next == ServicePlanProposalStatus.RETIRED
            ServicePlanProposalStatus.VALIDATED -> return next == ServicePlanProposalStatus.PUBLISHED || next == ServicePlanProposalStatus.RETIRED
            ServicePlanProposalStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanProposalStatus) {
        if (!canMoveTo(next)) {
            error("service plan proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((score - maximum) * 100) / 100

    fun snapshot(): ServicePlanProposal {
        return ServicePlanProposal(
            code = code,
            label = label,
            priority = priority,
            maximum = maximum,
            locale = locale,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service plan proposal in status created. */
fun newServicePlanProposal(code: String, label: String): ServicePlanProposal {
    return ServicePlanProposal(
        code = code,
        label = label,
        priority = 0,
        maximum = 0.0,
        locale = "",
        score = 0.0,
        status = ServicePlanProposalStatus.CREATED,
    )
}
