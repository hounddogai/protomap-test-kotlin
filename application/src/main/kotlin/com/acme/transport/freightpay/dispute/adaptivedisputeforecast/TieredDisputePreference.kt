package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.text.normalize

/** Where a tiered dispute preference is in its life cycle. */
enum class TieredDisputePreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered dispute preference. Its code is unique within a tenant. */
data class TieredDisputePreference(
    var code: String,
    var name: String,
    var createdAt: Int,
    var country: String,
    var carrier: String,
    var status: TieredDisputePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDisputePreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDisputePreferenceStatus): Boolean {
        when (status) {
            TieredDisputePreferenceStatus.PROPOSED -> {
                when (next) {
                    TieredDisputePreferenceStatus.APPROVED, TieredDisputePreferenceStatus.REJECTED -> return true
                }
                return false
            }
            TieredDisputePreferenceStatus.APPROVED -> return next == TieredDisputePreferenceStatus.WITHDRAWN
            TieredDisputePreferenceStatus.REJECTED -> {
                when (next) {
                    TieredDisputePreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDisputePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered dispute preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): TieredDisputePreference {
        return TieredDisputePreference(
            code = code,
            name = name,
            createdAt = createdAt,
            country = country,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered dispute preference in status proposed. */
fun newTieredDisputePreference(code: String, name: String): TieredDisputePreference {
    return TieredDisputePreference(
        code = code,
        name = name,
        createdAt = 0,
        country = "",
        carrier = "",
        status = TieredDisputePreferenceStatus.PROPOSED,
    )
}
