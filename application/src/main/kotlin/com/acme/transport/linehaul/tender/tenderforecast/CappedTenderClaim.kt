package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tender claim is in its life cycle. */
enum class CappedTenderClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped tender claim. Its code is unique within a tenant. */
data class CappedTenderClaim(
    var code: String,
    var name: String,
    var active: Boolean,
    var etaMinutes: Int,
    var margin: Double,
    var startsAt: Int,
    var status: CappedTenderClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTenderClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTenderClaimStatus): Boolean {
        when (status) {
            CappedTenderClaimStatus.REQUESTED -> {
                when (next) {
                    CappedTenderClaimStatus.GRANTED -> return true
                }
                return false
            }
            CappedTenderClaimStatus.GRANTED -> {
                when (next) {
                    CappedTenderClaimStatus.REVOKED, CappedTenderClaimStatus.EXPIRED -> return true
                }
                return false
            }
            CappedTenderClaimStatus.REVOKED -> return next == CappedTenderClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedTenderClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped tender claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped tender claim"} $code"

    fun effectiveValue(): Double = round((startsAt.toDouble() + margin) * 100) / 100

    fun snapshot(): CappedTenderClaim {
        return CappedTenderClaim(
            code = code,
            name = name,
            active = active,
            etaMinutes = etaMinutes,
            margin = margin,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped tender claim in status requested. */
fun newCappedTenderClaim(code: String, name: String): CappedTenderClaim {
    return CappedTenderClaim(
        code = code,
        name = name,
        active = false,
        etaMinutes = 0,
        margin = 0.0,
        startsAt = 0,
        status = CappedTenderClaimStatus.REQUESTED,
    )
}
