package com.acme.transport.customs.clearance.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback clearance claim is in its life cycle. */
enum class FallbackClearanceClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback clearance claim. Its code is unique within a tenant. */
data class FallbackClearanceClaim(
    var code: String,
    var label: String,
    var stopCount: Int,
    var capacity: Int,
    var etaMinutes: Int,
    var ratio: Double,
    var shiftCode: String,
    var createdAt: Int,
    var status: FallbackClearanceClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceClaimStatus): Boolean {
        when (status) {
            FallbackClearanceClaimStatus.REQUESTED -> return next == FallbackClearanceClaimStatus.REVOKED
            FallbackClearanceClaimStatus.GRANTED -> {
                when (next) {
                    FallbackClearanceClaimStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackClearanceClaimStatus.REVOKED -> return next == FallbackClearanceClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackClearanceClaimStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((stopCount.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackClearanceClaim {
        return FallbackClearanceClaim(
            code = code,
            label = label,
            stopCount = stopCount,
            capacity = capacity,
            etaMinutes = etaMinutes,
            ratio = ratio,
            shiftCode = shiftCode,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback clearance claim in status requested. */
fun newFallbackClearanceClaim(code: String, label: String): FallbackClearanceClaim {
    return FallbackClearanceClaim(
        code = code,
        label = label,
        stopCount = 0,
        capacity = 0,
        etaMinutes = 0,
        ratio = 0.0,
        shiftCode = "",
        createdAt = 0,
        status = FallbackClearanceClaimStatus.REQUESTED,
    )
}
