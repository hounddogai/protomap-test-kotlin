package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff variance is in its life cycle. */
enum class TariffVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tariff variance. Its code is unique within a tenant. */
data class TariffVariance(
    var code: String,
    var label: String,
    var discount: Double,
    var segment: String,
    var reviewer: String,
    var score: Double,
    var startsAt: Int,
    var capacity: Int,
    var status: TariffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffVarianceStatus): Boolean {
        when (status) {
            TariffVarianceStatus.PROPOSED -> {
                when (next) {
                    TariffVarianceStatus.REJECTED, TariffVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            TariffVarianceStatus.APPROVED -> {
                when (next) {
                    TariffVarianceStatus.REJECTED, TariffVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            TariffVarianceStatus.REJECTED -> {
                when (next) {
                    TariffVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tariff variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round(discount * capacity.toDouble() * 100) / 100

    fun snapshot(): TariffVariance {
        return TariffVariance(
            code = code,
            label = label,
            discount = discount,
            segment = segment,
            reviewer = reviewer,
            score = score,
            startsAt = startsAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tariff variance in status proposed. */
fun newTariffVariance(code: String, label: String): TariffVariance {
    return TariffVariance(
        code = code,
        label = label,
        discount = 0.0,
        segment = "",
        reviewer = "",
        score = 0.0,
        startsAt = 0,
        capacity = 0,
        status = TariffVarianceStatus.PROPOSED,
    )
}
