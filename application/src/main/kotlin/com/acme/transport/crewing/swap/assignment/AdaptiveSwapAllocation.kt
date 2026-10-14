package com.acme.transport.crewing.swap.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap allocation is in its life cycle. */
enum class AdaptiveSwapAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive swap allocation, with what the planners need to act on it. */
data class AdaptiveSwapAllocation(
    var code: String,
    var label: String,
    var createdAt: Int,
    var loadKg: Double,
    var revision: Int,
    var approved: Boolean,
    var country: String,
    var quantity: Int,
    var pallets: Int,
    var status: AdaptiveSwapAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapAllocationStatus): Boolean {
        when (status) {
            AdaptiveSwapAllocationStatus.DRAFT -> return next == AdaptiveSwapAllocationStatus.ACTIVE
            AdaptiveSwapAllocationStatus.PENDING -> {
                when (next) {
                    AdaptiveSwapAllocationStatus.ACTIVE, AdaptiveSwapAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveSwapAllocationStatus.ACTIVE -> return next == AdaptiveSwapAllocationStatus.SUSPENDED || next == AdaptiveSwapAllocationStatus.CLOSED
            AdaptiveSwapAllocationStatus.SUSPENDED -> return next == AdaptiveSwapAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"adaptive swap allocation"} $code"

    fun effectiveTotal(): Double = round((pallets.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveSwapAllocation {
        return AdaptiveSwapAllocation(
            code = code,
            label = label,
            createdAt = createdAt,
            loadKg = loadKg,
            revision = revision,
            approved = approved,
            country = country,
            quantity = quantity,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive swap allocation in status draft. */
fun newAdaptiveSwapAllocation(code: String, label: String): AdaptiveSwapAllocation {
    return AdaptiveSwapAllocation(
        code = code,
        label = label,
        createdAt = 0,
        loadKg = 0.0,
        revision = 0,
        approved = false,
        country = "",
        quantity = 0,
        pallets = 0,
        status = AdaptiveSwapAllocationStatus.DRAFT,
    )
}
