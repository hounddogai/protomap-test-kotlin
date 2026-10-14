package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.text.normalize

// The adaptive service area claim records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a adaptive service area claim is in its life cycle. */
enum class AdaptiveServiceAreaClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive service area claim. Its code is unique within a tenant. */
data class AdaptiveServiceAreaClaim(
    var code: String,
    var title: String,
    var phone: String,
    var reviewer: String,
    var attempts: Int,
    var name: String,
    var status: AdaptiveServiceAreaClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServiceAreaClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServiceAreaClaimStatus): Boolean {
        when (status) {
            AdaptiveServiceAreaClaimStatus.PLANNED -> return next == AdaptiveServiceAreaClaimStatus.CANCELLED
            AdaptiveServiceAreaClaimStatus.SCHEDULED -> return next == AdaptiveServiceAreaClaimStatus.IN_PROGRESS || next == AdaptiveServiceAreaClaimStatus.COMPLETED
            AdaptiveServiceAreaClaimStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveServiceAreaClaimStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveServiceAreaClaimStatus.COMPLETED -> {
                when (next) {
                    AdaptiveServiceAreaClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServiceAreaClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service area claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive service area claim"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): AdaptiveServiceAreaClaim {
        return AdaptiveServiceAreaClaim(
            code = code,
            title = title,
            phone = phone,
            reviewer = reviewer,
            attempts = attempts,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive service area claim in status planned. */
fun newAdaptiveServiceAreaClaim(code: String, title: String): AdaptiveServiceAreaClaim {
    return AdaptiveServiceAreaClaim(
        code = code,
        title = title,
        phone = "",
        reviewer = "",
        attempts = 0,
        name = "",
        status = AdaptiveServiceAreaClaimStatus.PLANNED,
    )
}
