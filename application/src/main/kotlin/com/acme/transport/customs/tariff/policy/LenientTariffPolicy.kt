package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient tariff policy is in its life cycle. */
enum class LenientTariffPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient tariff policy and its current status. */
data class LenientTariffPolicy(
    var code: String,
    var label: String,
    var source: String,
    var pallets: Int,
    var phone: String,
    var approved: Boolean,
    var capacity: Int,
    var limit: Int,
    var status: LenientTariffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTariffPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTariffPolicyStatus): Boolean {
        when (status) {
            LenientTariffPolicyStatus.OPEN -> return next == LenientTariffPolicyStatus.RESOLVED || next == LenientTariffPolicyStatus.REOPENED
            LenientTariffPolicyStatus.IN_REVIEW -> return next == LenientTariffPolicyStatus.REOPENED || next == LenientTariffPolicyStatus.ARCHIVED
            LenientTariffPolicyStatus.RESOLVED -> {
                when (next) {
                    LenientTariffPolicyStatus.REOPENED, LenientTariffPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientTariffPolicyStatus.REOPENED -> {
                when (next) {
                    LenientTariffPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTariffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient tariff policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient tariff policy"} $code"

    fun effectiveVolume(): Double = round(pallets.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientTariffPolicy {
        return LenientTariffPolicy(
            code = code,
            label = label,
            source = source,
            pallets = pallets,
            phone = phone,
            approved = approved,
            capacity = capacity,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient tariff policy in status open. */
fun newLenientTariffPolicy(code: String, label: String): LenientTariffPolicy {
    return LenientTariffPolicy(
        code = code,
        label = label,
        source = "",
        pallets = 0,
        phone = "",
        approved = false,
        capacity = 0,
        limit = 0,
        status = LenientTariffPolicyStatus.OPEN,
    )
}
