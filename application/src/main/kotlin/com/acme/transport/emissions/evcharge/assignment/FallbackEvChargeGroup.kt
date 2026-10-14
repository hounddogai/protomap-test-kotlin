package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.normalize

/** Where a fallback ev charge group is in its life cycle. */
enum class FallbackEvChargeGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback ev charge group and its current status. */
data class FallbackEvChargeGroup(
    var code: String,
    var title: String,
    var category: String,
    var locale: String,
    var approved: Boolean,
    var margin: Double,
    var status: FallbackEvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeGroupStatus): Boolean {
        when (status) {
            FallbackEvChargeGroupStatus.REQUESTED -> return next == FallbackEvChargeGroupStatus.GRANTED || next == FallbackEvChargeGroupStatus.REVOKED
            FallbackEvChargeGroupStatus.GRANTED -> return next == FallbackEvChargeGroupStatus.EXPIRED
            FallbackEvChargeGroupStatus.REVOKED -> {
                when (next) {
                    FallbackEvChargeGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback ev charge group"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): FallbackEvChargeGroup {
        return FallbackEvChargeGroup(
            code = code,
            title = title,
            category = category,
            locale = locale,
            approved = approved,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback ev charge group in status requested. */
fun newFallbackEvChargeGroup(code: String, title: String): FallbackEvChargeGroup {
    return FallbackEvChargeGroup(
        code = code,
        title = title,
        category = "",
        locale = "",
        approved = false,
        margin = 0.0,
        status = FallbackEvChargeGroupStatus.REQUESTED,
    )
}
