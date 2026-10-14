package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched tariff digest records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a batched tariff digest is in its life cycle. */
enum class BatchedTariffDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched tariff digest and its current status. */
data class BatchedTariffDigest(
    var code: String,
    var title: String,
    var carrier: String,
    var locale: String,
    var depot: String,
    var etaMinutes: Int,
    var name: String,
    var currency: String,
    var visible: Boolean,
    var cost: Double,
    var status: BatchedTariffDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffDigestStatus): Boolean {
        when (status) {
            BatchedTariffDigestStatus.PLANNED -> {
                when (next) {
                    BatchedTariffDigestStatus.IN_PROGRESS, BatchedTariffDigestStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedTariffDigestStatus.SCHEDULED -> {
                when (next) {
                    BatchedTariffDigestStatus.IN_PROGRESS, BatchedTariffDigestStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedTariffDigestStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedTariffDigestStatus.COMPLETED, BatchedTariffDigestStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedTariffDigestStatus.COMPLETED -> return next == BatchedTariffDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedTariffDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched tariff digest"} $code"

    fun projectedExposure(): Double = round((etaMinutes.toDouble() - cost) * 100) / 100

    fun snapshot(): BatchedTariffDigest {
        return BatchedTariffDigest(
            code = code,
            title = title,
            carrier = carrier,
            locale = locale,
            depot = depot,
            etaMinutes = etaMinutes,
            name = name,
            currency = currency,
            visible = visible,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched tariff digest in status planned. */
fun newBatchedTariffDigest(code: String, title: String): BatchedTariffDigest {
    return BatchedTariffDigest(
        code = code,
        title = title,
        carrier = "",
        locale = "",
        depot = "",
        etaMinutes = 0,
        name = "",
        currency = "",
        visible = false,
        cost = 0.0,
        status = BatchedTariffDigestStatus.PLANNED,
    )
}
