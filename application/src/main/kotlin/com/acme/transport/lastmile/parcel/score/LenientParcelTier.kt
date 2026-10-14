package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel tier is in its life cycle. */
enum class LenientParcelTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lenient parcel tier and its current status. */
data class LenientParcelTier(
    var code: String,
    var title: String,
    var budget: Double,
    var discount: Double,
    var tags: MutableList<String>,
    var status: LenientParcelTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelTierStatus): Boolean {
        when (status) {
            LenientParcelTierStatus.PROPOSED -> {
                when (next) {
                    LenientParcelTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientParcelTierStatus.APPROVED -> {
                when (next) {
                    LenientParcelTierStatus.REJECTED -> return true
                }
                return false
            }
            LenientParcelTierStatus.REJECTED -> return next == LenientParcelTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientParcelTierStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient parcel tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(discount * budget * 100) / 100

    fun snapshot(): LenientParcelTier {
        return LenientParcelTier(
            code = code,
            title = title,
            budget = budget,
            discount = discount,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient parcel tier in status proposed. */
fun newLenientParcelTier(code: String, title: String): LenientParcelTier {
    return LenientParcelTier(
        code = code,
        title = title,
        budget = 0.0,
        discount = 0.0,
        tags = mutableListOf<String>(),
        status = LenientParcelTierStatus.PROPOSED,
    )
}
