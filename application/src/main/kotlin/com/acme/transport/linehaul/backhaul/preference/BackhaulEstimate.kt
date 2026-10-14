package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul estimate is in its life cycle. */
enum class BackhaulEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A backhaul estimate as the backhaul team keeps it. */
data class BackhaulEstimate(
    var code: String,
    var name: String,
    var amount: Double,
    var approved: Boolean,
    var country: String,
    var summary: String,
    var attempts: Int,
    var lane: String,
    var depot: String,
    var loadKg: Double,
    var region: String,
    var status: BackhaulEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulEstimateStatus): Boolean {
        when (status) {
            BackhaulEstimateStatus.REQUESTED -> {
                when (next) {
                    BackhaulEstimateStatus.REVOKED -> return true
                }
                return false
            }
            BackhaulEstimateStatus.GRANTED -> {
                when (next) {
                    BackhaulEstimateStatus.REVOKED, BackhaulEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            BackhaulEstimateStatus.REVOKED -> {
                when (next) {
                    BackhaulEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulEstimateStatus) {
        if (!canMoveTo(next)) {
            error("backhaul estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((attempts.toDouble() + amount) * 100) / 100

    fun snapshot(): BackhaulEstimate {
        return BackhaulEstimate(
            code = code,
            name = name,
            amount = amount,
            approved = approved,
            country = country,
            summary = summary,
            attempts = attempts,
            lane = lane,
            depot = depot,
            loadKg = loadKg,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a backhaul estimate in status requested. */
fun newBackhaulEstimate(code: String, name: String): BackhaulEstimate {
    return BackhaulEstimate(
        code = code,
        name = name,
        amount = 0.0,
        approved = false,
        country = "",
        summary = "",
        attempts = 0,
        lane = "",
        depot = "",
        loadKg = 0.0,
        region = "",
        status = BackhaulEstimateStatus.REQUESTED,
    )
}
