package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan claim is in its life cycle. */
enum class ServicePlanClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service plan claim. Its code is unique within a tenant. */
data class ServicePlanClaim(
    var code: String,
    var label: String,
    var createdAt: Int,
    var revision: Int,
    var notes: String,
    var country: String,
    var status: ServicePlanClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanClaimStatus): Boolean {
        when (status) {
            ServicePlanClaimStatus.PROPOSED -> return next == ServicePlanClaimStatus.APPROVED
            ServicePlanClaimStatus.APPROVED -> return next == ServicePlanClaimStatus.REJECTED || next == ServicePlanClaimStatus.WITHDRAWN
            ServicePlanClaimStatus.REJECTED -> return next == ServicePlanClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServicePlanClaimStatus) {
        if (!canMoveTo(next)) {
            error("service plan claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((revision.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanClaim {
        return ServicePlanClaim(
            code = code,
            label = label,
            createdAt = createdAt,
            revision = revision,
            notes = notes,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a service plan claim in status proposed. */
fun newServicePlanClaim(code: String, label: String): ServicePlanClaim {
    return ServicePlanClaim(
        code = code,
        label = label,
        createdAt = 0,
        revision = 0,
        notes = "",
        country = "",
        status = ServicePlanClaimStatus.PROPOSED,
    )
}
