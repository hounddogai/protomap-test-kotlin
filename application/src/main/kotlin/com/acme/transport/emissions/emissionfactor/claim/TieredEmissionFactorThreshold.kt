package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered emission factor threshold is in its life cycle. */
enum class TieredEmissionFactorThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One tiered emission factor threshold, with what the planners need to act on it. */
data class TieredEmissionFactorThreshold(
    var code: String,
    var title: String,
    var cost: Double,
    var region: String,
    var graceDays: Int,
    var batchSize: Int,
    var locale: String,
    var status: TieredEmissionFactorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorThresholdStatus): Boolean {
        when (status) {
            TieredEmissionFactorThresholdStatus.PROPOSED -> return next == TieredEmissionFactorThresholdStatus.REJECTED
            TieredEmissionFactorThresholdStatus.APPROVED -> return next == TieredEmissionFactorThresholdStatus.REJECTED || next == TieredEmissionFactorThresholdStatus.WITHDRAWN
            TieredEmissionFactorThresholdStatus.REJECTED -> return next == TieredEmissionFactorThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered emission factor threshold"} $code"

    fun effectiveTotal(): Double = round((graceDays.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): TieredEmissionFactorThreshold {
        return TieredEmissionFactorThreshold(
            code = code,
            title = title,
            cost = cost,
            region = region,
            graceDays = graceDays,
            batchSize = batchSize,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered emission factor threshold in status proposed. */
fun newTieredEmissionFactorThreshold(code: String, title: String): TieredEmissionFactorThreshold {
    return TieredEmissionFactorThreshold(
        code = code,
        title = title,
        cost = 0.0,
        region = "",
        graceDays = 0,
        batchSize = 0,
        locale = "",
        status = TieredEmissionFactorThresholdStatus.PROPOSED,
    )
}
