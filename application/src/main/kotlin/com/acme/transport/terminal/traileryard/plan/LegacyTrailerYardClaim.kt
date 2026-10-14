package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer yard claim is in its life cycle. */
enum class LegacyTrailerYardClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy trailer yard claim as the trailer yard team keeps it. */
data class LegacyTrailerYardClaim(
    var code: String,
    var name: String,
    var visible: Boolean,
    var attempts: Int,
    var batchSize: Int,
    var description: String,
    var carrier: String,
    var loadKg: Double,
    var timezone: String,
    var shiftCode: String,
    var threshold: Double,
    var status: LegacyTrailerYardClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardClaimStatus): Boolean {
        when (status) {
            LegacyTrailerYardClaimStatus.OPEN -> return next == LegacyTrailerYardClaimStatus.IN_REVIEW || next == LegacyTrailerYardClaimStatus.ARCHIVED
            LegacyTrailerYardClaimStatus.IN_REVIEW -> {
                when (next) {
                    LegacyTrailerYardClaimStatus.REOPENED, LegacyTrailerYardClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyTrailerYardClaimStatus.RESOLVED -> return next == LegacyTrailerYardClaimStatus.REOPENED || next == LegacyTrailerYardClaimStatus.ARCHIVED
            LegacyTrailerYardClaimStatus.REOPENED -> {
                when (next) {
                    LegacyTrailerYardClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardClaimStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy trailer yard claim"} $code"

    fun weightedValue(): Double = round((loadKg - attempts.toDouble()) * 100) / 100

    fun snapshot(): LegacyTrailerYardClaim {
        return LegacyTrailerYardClaim(
            code = code,
            name = name,
            visible = visible,
            attempts = attempts,
            batchSize = batchSize,
            description = description,
            carrier = carrier,
            loadKg = loadKg,
            timezone = timezone,
            shiftCode = shiftCode,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy trailer yard claim in status open. */
fun newLegacyTrailerYardClaim(code: String, name: String): LegacyTrailerYardClaim {
    return LegacyTrailerYardClaim(
        code = code,
        name = name,
        visible = false,
        attempts = 0,
        batchSize = 0,
        description = "",
        carrier = "",
        loadKg = 0.0,
        timezone = "",
        shiftCode = "",
        threshold = 0.0,
        status = LegacyTrailerYardClaimStatus.OPEN,
    )
}
