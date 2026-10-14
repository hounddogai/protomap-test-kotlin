package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel policy is in its life cycle. */
enum class LenientParcelPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient parcel policy, with what the planners need to act on it. */
data class LenientParcelPolicy(
    var code: String,
    var name: String,
    var archived: Boolean,
    var fuelLiters: Double,
    var category: String,
    var maximum: Double,
    var status: LenientParcelPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelPolicyStatus): Boolean {
        when (status) {
            LenientParcelPolicyStatus.REQUESTED -> {
                when (next) {
                    LenientParcelPolicyStatus.GRANTED, LenientParcelPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            LenientParcelPolicyStatus.GRANTED -> {
                when (next) {
                    LenientParcelPolicyStatus.REVOKED, LenientParcelPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            LenientParcelPolicyStatus.REVOKED -> {
                when (next) {
                    LenientParcelPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientParcelPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient parcel policy"} $code"

    fun effectiveValue(): Double = round((fuelLiters - maximum) * 100) / 100

    fun snapshot(): LenientParcelPolicy {
        return LenientParcelPolicy(
            code = code,
            name = name,
            archived = archived,
            fuelLiters = fuelLiters,
            category = category,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient parcel policy in status requested. */
fun newLenientParcelPolicy(code: String, name: String): LenientParcelPolicy {
    return LenientParcelPolicy(
        code = code,
        name = name,
        archived = false,
        fuelLiters = 0.0,
        category = "",
        maximum = 0.0,
        status = LenientParcelPolicyStatus.REQUESTED,
    )
}
