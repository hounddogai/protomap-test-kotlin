package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient near miss claim is in its life cycle. */
enum class LenientNearMissClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient near miss claim. Its code is unique within a tenant. */
data class LenientNearMissClaim(
    var code: String,
    var title: String,
    var checksum: String,
    var label: String,
    var quantity: Int,
    var budget: Double,
    var discount: Double,
    var status: LenientNearMissClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissClaimStatus): Boolean {
        when (status) {
            LenientNearMissClaimStatus.PROPOSED -> {
                when (next) {
                    LenientNearMissClaimStatus.APPROVED -> return true
                }
                return false
            }
            LenientNearMissClaimStatus.APPROVED -> {
                when (next) {
                    LenientNearMissClaimStatus.REJECTED, LenientNearMissClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientNearMissClaimStatus.REJECTED -> {
                when (next) {
                    LenientNearMissClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientNearMissClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun netTotal(): Double = round((quantity.toDouble() + discount) * 100) / 100

    fun snapshot(): LenientNearMissClaim {
        return LenientNearMissClaim(
            code = code,
            title = title,
            checksum = checksum,
            label = label,
            quantity = quantity,
            budget = budget,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient near miss claim in status proposed. */
fun newLenientNearMissClaim(code: String, title: String): LenientNearMissClaim {
    return LenientNearMissClaim(
        code = code,
        title = title,
        checksum = "",
        label = "",
        quantity = 0,
        budget = 0.0,
        discount = 0.0,
        status = LenientNearMissClaimStatus.PROPOSED,
    )
}
