package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient clearance policy is in its life cycle. */
enum class LenientClearancePolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient clearance policy. Its code is unique within a tenant. */
data class LenientClearancePolicy(
    var code: String,
    var label: String,
    var discount: Double,
    var graceDays: Int,
    var currency: String,
    var version: Int,
    var notes: String,
    var timezone: String,
    var shiftCode: String,
    var status: LenientClearancePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientClearancePolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientClearancePolicyStatus): Boolean {
        when (status) {
            LenientClearancePolicyStatus.REQUESTED -> return next == LenientClearancePolicyStatus.GRANTED || next == LenientClearancePolicyStatus.EXPIRED
            LenientClearancePolicyStatus.GRANTED -> {
                when (next) {
                    LenientClearancePolicyStatus.EXPIRED -> return true
                }
                return false
            }
            LenientClearancePolicyStatus.REVOKED -> return next == LenientClearancePolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientClearancePolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient clearance policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"lenient clearance policy"} $code"

    fun effectiveTotal(): Double = round(version.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): LenientClearancePolicy {
        return LenientClearancePolicy(
            code = code,
            label = label,
            discount = discount,
            graceDays = graceDays,
            currency = currency,
            version = version,
            notes = notes,
            timezone = timezone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient clearance policy in status requested. */
fun newLenientClearancePolicy(code: String, label: String): LenientClearancePolicy {
    return LenientClearancePolicy(
        code = code,
        label = label,
        discount = 0.0,
        graceDays = 0,
        currency = "",
        version = 0,
        notes = "",
        timezone = "",
        shiftCode = "",
        status = LenientClearancePolicyStatus.REQUESTED,
    )
}
