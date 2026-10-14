package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff tier is in its life cycle. */
enum class TariffTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff tier. Its code is unique within a tenant. */
data class TariffTier(
    var code: String,
    var title: String,
    var depot: String,
    var revision: Int,
    var etaMinutes: Int,
    var budget: Double,
    var minimum: Double,
    var stopCount: Int,
    var status: TariffTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffTierStatus): Boolean {
        when (status) {
            TariffTierStatus.PLANNED -> {
                when (next) {
                    TariffTierStatus.IN_PROGRESS, TariffTierStatus.COMPLETED -> return true
                }
                return false
            }
            TariffTierStatus.SCHEDULED -> return next == TariffTierStatus.COMPLETED || next == TariffTierStatus.CANCELLED
            TariffTierStatus.IN_PROGRESS -> return next == TariffTierStatus.COMPLETED
            TariffTierStatus.COMPLETED -> return next == TariffTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TariffTierStatus) {
        if (!canMoveTo(next)) {
            error("tariff tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tariff tier"} $code"

    fun weightedLoad(): Double = round((budget + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TariffTier {
        return TariffTier(
            code = code,
            title = title,
            depot = depot,
            revision = revision,
            etaMinutes = etaMinutes,
            budget = budget,
            minimum = minimum,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tariff tier in status planned. */
fun newTariffTier(code: String, title: String): TariffTier {
    return TariffTier(
        code = code,
        title = title,
        depot = "",
        revision = 0,
        etaMinutes = 0,
        budget = 0.0,
        minimum = 0.0,
        stopCount = 0,
        status = TariffTierStatus.PLANNED,
    )
}
