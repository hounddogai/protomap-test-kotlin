package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return tier is in its life cycle. */
enum class AdaptiveLeaseReturnTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive lease return tier as the lease return team keeps it. */
data class AdaptiveLeaseReturnTier(
    var code: String,
    var title: String,
    var cost: Double,
    var reviewer: String,
    var attempts: Int,
    var graceDays: Int,
    var label: String,
    var archived: Boolean,
    var status: AdaptiveLeaseReturnTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnTierStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnTierStatus.NEW -> return next == AdaptiveLeaseReturnTierStatus.RUNNING || next == AdaptiveLeaseReturnTierStatus.DONE
            AdaptiveLeaseReturnTierStatus.QUEUED -> {
                when (next) {
                    AdaptiveLeaseReturnTierStatus.RUNNING, AdaptiveLeaseReturnTierStatus.DONE -> return true
                }
                return false
            }
            AdaptiveLeaseReturnTierStatus.RUNNING -> {
                when (next) {
                    AdaptiveLeaseReturnTierStatus.DONE -> return true
                }
                return false
            }
            AdaptiveLeaseReturnTierStatus.DONE -> {
                when (next) {
                    AdaptiveLeaseReturnTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnTierStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun effectiveExposure(): Double = round((cost - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnTier {
        return AdaptiveLeaseReturnTier(
            code = code,
            title = title,
            cost = cost,
            reviewer = reviewer,
            attempts = attempts,
            graceDays = graceDays,
            label = label,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive lease return tier in status new. */
fun newAdaptiveLeaseReturnTier(code: String, title: String): AdaptiveLeaseReturnTier {
    return AdaptiveLeaseReturnTier(
        code = code,
        title = title,
        cost = 0.0,
        reviewer = "",
        attempts = 0,
        graceDays = 0,
        label = "",
        archived = false,
        status = AdaptiveLeaseReturnTierStatus.NEW,
    )
}
