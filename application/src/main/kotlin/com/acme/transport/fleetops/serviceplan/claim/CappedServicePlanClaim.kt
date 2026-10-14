package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped service plan claim records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a capped service plan claim is in its life cycle. */
enum class CappedServicePlanClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped service plan claim. Its code is unique within a tenant. */
data class CappedServicePlanClaim(
    var code: String,
    var title: String,
    var reviewer: String,
    var expiresAt: Int,
    var minimum: Double,
    var status: CappedServicePlanClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServicePlanClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServicePlanClaimStatus): Boolean {
        when (status) {
            CappedServicePlanClaimStatus.NEW -> {
                when (next) {
                    CappedServicePlanClaimStatus.QUEUED, CappedServicePlanClaimStatus.RUNNING -> return true
                }
                return false
            }
            CappedServicePlanClaimStatus.QUEUED -> {
                when (next) {
                    CappedServicePlanClaimStatus.DONE, CappedServicePlanClaimStatus.FAILED -> return true
                }
                return false
            }
            CappedServicePlanClaimStatus.RUNNING -> {
                when (next) {
                    CappedServicePlanClaimStatus.DONE, CappedServicePlanClaimStatus.FAILED -> return true
                }
                return false
            }
            CappedServicePlanClaimStatus.DONE -> return next == CappedServicePlanClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedServicePlanClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped service plan claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((expiresAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): CappedServicePlanClaim {
        return CappedServicePlanClaim(
            code = code,
            title = title,
            reviewer = reviewer,
            expiresAt = expiresAt,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped service plan claim in status new. */
fun newCappedServicePlanClaim(code: String, title: String): CappedServicePlanClaim {
    return CappedServicePlanClaim(
        code = code,
        title = title,
        reviewer = "",
        expiresAt = 0,
        minimum = 0.0,
        status = CappedServicePlanClaimStatus.NEW,
    )
}
