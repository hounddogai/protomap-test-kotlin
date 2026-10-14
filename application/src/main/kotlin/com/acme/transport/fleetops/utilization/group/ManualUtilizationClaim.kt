package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual utilization claim is in its life cycle. */
enum class ManualUtilizationClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One manual utilization claim, with what the planners need to act on it. */
data class ManualUtilizationClaim(
    var code: String,
    var label: String,
    var budget: Double,
    var shiftCode: String,
    var channel: String,
    var loadKg: Double,
    var createdAt: Int,
    var status: ManualUtilizationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationClaimStatus): Boolean {
        when (status) {
            ManualUtilizationClaimStatus.REQUESTED -> {
                when (next) {
                    ManualUtilizationClaimStatus.REVOKED -> return true
                }
                return false
            }
            ManualUtilizationClaimStatus.GRANTED -> return next == ManualUtilizationClaimStatus.EXPIRED
            ManualUtilizationClaimStatus.REVOKED -> {
                when (next) {
                    ManualUtilizationClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualUtilizationClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round(loadKg * createdAt.toDouble() * 100) / 100

    fun snapshot(): ManualUtilizationClaim {
        return ManualUtilizationClaim(
            code = code,
            label = label,
            budget = budget,
            shiftCode = shiftCode,
            channel = channel,
            loadKg = loadKg,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual utilization claim in status requested. */
fun newManualUtilizationClaim(code: String, label: String): ManualUtilizationClaim {
    return ManualUtilizationClaim(
        code = code,
        label = label,
        budget = 0.0,
        shiftCode = "",
        channel = "",
        loadKg = 0.0,
        createdAt = 0,
        status = ManualUtilizationClaimStatus.REQUESTED,
    )
}
