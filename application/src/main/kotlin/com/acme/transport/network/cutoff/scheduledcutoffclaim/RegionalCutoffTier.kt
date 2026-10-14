package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cutoff tier is in its life cycle. */
enum class RegionalCutoffTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional cutoff tier and its current status. */
data class RegionalCutoffTier(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var score: Double,
    var version: Int,
    var pallets: Int,
    var status: RegionalCutoffTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffTierStatus): Boolean {
        when (status) {
            RegionalCutoffTierStatus.REQUESTED -> return next == RegionalCutoffTierStatus.REVOKED
            RegionalCutoffTierStatus.GRANTED -> {
                when (next) {
                    RegionalCutoffTierStatus.REVOKED -> return true
                }
                return false
            }
            RegionalCutoffTierStatus.REVOKED -> {
                when (next) {
                    RegionalCutoffTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCutoffTierStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"regional cutoff tier"} $code"

    fun adjustedVolume(): Double = round(fuelLiters * pallets.toDouble() * 100) / 100

    fun snapshot(): RegionalCutoffTier {
        return RegionalCutoffTier(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            score = score,
            version = version,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional cutoff tier in status requested. */
fun newRegionalCutoffTier(code: String, title: String): RegionalCutoffTier {
    return RegionalCutoffTier(
        code = code,
        title = title,
        fuelLiters = 0.0,
        score = 0.0,
        version = 0,
        pallets = 0,
        status = RegionalCutoffTierStatus.REQUESTED,
    )
}
