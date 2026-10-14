package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive hub proposal is in its life cycle. */
enum class AdaptiveHubProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive hub proposal. Its code is unique within a tenant. */
data class AdaptiveHubProposal(
    var code: String,
    var name: String,
    var label: String,
    var weight: Double,
    var unit: String,
    var phone: String,
    var distanceKm: Double,
    var pinned: Boolean,
    var windowDays: Int,
    var threshold: Double,
    var fuelLiters: Double,
    var status: AdaptiveHubProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHubProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHubProposalStatus): Boolean {
        when (status) {
            AdaptiveHubProposalStatus.IDLE -> return next == AdaptiveHubProposalStatus.WARMING || next == AdaptiveHubProposalStatus.DRAINING
            AdaptiveHubProposalStatus.WARMING -> {
                when (next) {
                    AdaptiveHubProposalStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveHubProposalStatus.READY -> return next == AdaptiveHubProposalStatus.DRAINING || next == AdaptiveHubProposalStatus.STOPPED
            AdaptiveHubProposalStatus.DRAINING -> return next == AdaptiveHubProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveHubProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hub proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun netValue(): Double = round((threshold + weight) * 100) / 100

    fun snapshot(): AdaptiveHubProposal {
        return AdaptiveHubProposal(
            code = code,
            name = name,
            label = label,
            weight = weight,
            unit = unit,
            phone = phone,
            distanceKm = distanceKm,
            pinned = pinned,
            windowDays = windowDays,
            threshold = threshold,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive hub proposal in status idle. */
fun newAdaptiveHubProposal(code: String, name: String): AdaptiveHubProposal {
    return AdaptiveHubProposal(
        code = code,
        name = name,
        label = "",
        weight = 0.0,
        unit = "",
        phone = "",
        distanceKm = 0.0,
        pinned = false,
        windowDays = 0,
        threshold = 0.0,
        fuelLiters = 0.0,
        status = AdaptiveHubProposalStatus.IDLE,
    )
}
