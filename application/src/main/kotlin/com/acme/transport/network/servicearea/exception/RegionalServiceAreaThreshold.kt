package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service area threshold is in its life cycle. */
enum class RegionalServiceAreaThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional service area threshold. Its code is unique within a tenant. */
data class RegionalServiceAreaThreshold(
    var code: String,
    var name: String,
    var score: Double,
    var margin: Double,
    var discount: Double,
    var category: String,
    var status: RegionalServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaThresholdStatus): Boolean {
        when (status) {
            RegionalServiceAreaThresholdStatus.REQUESTED -> {
                when (next) {
                    RegionalServiceAreaThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalServiceAreaThresholdStatus.GRANTED -> return next == RegionalServiceAreaThresholdStatus.REVOKED || next == RegionalServiceAreaThresholdStatus.EXPIRED
            RegionalServiceAreaThresholdStatus.REVOKED -> return next == RegionalServiceAreaThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional service area threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(margin * discount * 100) / 100

    fun snapshot(): RegionalServiceAreaThreshold {
        return RegionalServiceAreaThreshold(
            code = code,
            name = name,
            score = score,
            margin = margin,
            discount = discount,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional service area threshold in status requested. */
fun newRegionalServiceAreaThreshold(code: String, name: String): RegionalServiceAreaThreshold {
    return RegionalServiceAreaThreshold(
        code = code,
        name = name,
        score = 0.0,
        margin = 0.0,
        discount = 0.0,
        category = "",
        status = RegionalServiceAreaThresholdStatus.REQUESTED,
    )
}
