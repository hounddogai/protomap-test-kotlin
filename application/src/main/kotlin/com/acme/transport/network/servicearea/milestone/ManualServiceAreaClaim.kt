package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service area claim is in its life cycle. */
enum class ManualServiceAreaClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual service area claim, with what the planners need to act on it. */
data class ManualServiceAreaClaim(
    var code: String,
    var name: String,
    var label: String,
    var startsAt: Int,
    var batchSize: Int,
    var budget: Double,
    var trailerId: String,
    var category: String,
    var country: String,
    var externalRef: String,
    var status: ManualServiceAreaClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServiceAreaClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServiceAreaClaimStatus): Boolean {
        when (status) {
            ManualServiceAreaClaimStatus.PROPOSED -> {
                when (next) {
                    ManualServiceAreaClaimStatus.APPROVED, ManualServiceAreaClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualServiceAreaClaimStatus.APPROVED -> {
                when (next) {
                    ManualServiceAreaClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualServiceAreaClaimStatus.REJECTED -> {
                when (next) {
                    ManualServiceAreaClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualServiceAreaClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual service area claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedTotal(): Double = round(budget * batchSize.toDouble() * 100) / 100

    fun snapshot(): ManualServiceAreaClaim {
        return ManualServiceAreaClaim(
            code = code,
            name = name,
            label = label,
            startsAt = startsAt,
            batchSize = batchSize,
            budget = budget,
            trailerId = trailerId,
            category = category,
            country = country,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual service area claim in status proposed. */
fun newManualServiceAreaClaim(code: String, name: String): ManualServiceAreaClaim {
    return ManualServiceAreaClaim(
        code = code,
        name = name,
        label = "",
        startsAt = 0,
        batchSize = 0,
        budget = 0.0,
        trailerId = "",
        category = "",
        country = "",
        externalRef = "",
        status = ManualServiceAreaClaimStatus.PROPOSED,
    )
}
