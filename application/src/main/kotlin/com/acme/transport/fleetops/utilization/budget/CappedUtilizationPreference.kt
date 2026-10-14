package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped utilization preference is in its life cycle. */
enum class CappedUtilizationPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped utilization preference. Its code is unique within a tenant. */
data class CappedUtilizationPreference(
    var code: String,
    var name: String,
    var amount: Double,
    var retries: Int,
    var country: String,
    var status: CappedUtilizationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedUtilizationPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedUtilizationPreferenceStatus): Boolean {
        when (status) {
            CappedUtilizationPreferenceStatus.PROPOSED -> {
                when (next) {
                    CappedUtilizationPreferenceStatus.APPROVED -> return true
                }
                return false
            }
            CappedUtilizationPreferenceStatus.APPROVED -> return next == CappedUtilizationPreferenceStatus.WITHDRAWN
            CappedUtilizationPreferenceStatus.REJECTED -> {
                when (next) {
                    CappedUtilizationPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedUtilizationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped utilization preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((retries.toDouble() + amount) * 100) / 100

    fun snapshot(): CappedUtilizationPreference {
        return CappedUtilizationPreference(
            code = code,
            name = name,
            amount = amount,
            retries = retries,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped utilization preference in status proposed. */
fun newCappedUtilizationPreference(code: String, name: String): CappedUtilizationPreference {
    return CappedUtilizationPreference(
        code = code,
        name = name,
        amount = 0.0,
        retries = 0,
        country = "",
        status = CappedUtilizationPreferenceStatus.PROPOSED,
    )
}
