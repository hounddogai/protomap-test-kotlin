package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tariff batch is in its life cycle. */
enum class DefaultTariffBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default tariff batch and its current status. */
data class DefaultTariffBatch(
    var code: String,
    var title: String,
    var region: String,
    var checksum: String,
    var trailerId: String,
    var budget: Double,
    var startsAt: Int,
    var maximum: Double,
    var discount: Double,
    var status: DefaultTariffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffBatchStatus): Boolean {
        when (status) {
            DefaultTariffBatchStatus.CREATED -> {
                when (next) {
                    DefaultTariffBatchStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultTariffBatchStatus.VALIDATED -> return next == DefaultTariffBatchStatus.RETIRED
            DefaultTariffBatchStatus.PUBLISHED -> return next == DefaultTariffBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultTariffBatchStatus) {
        if (!canMoveTo(next)) {
            error("default tariff batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((discount + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultTariffBatch {
        return DefaultTariffBatch(
            code = code,
            title = title,
            region = region,
            checksum = checksum,
            trailerId = trailerId,
            budget = budget,
            startsAt = startsAt,
            maximum = maximum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default tariff batch in status created. */
fun newDefaultTariffBatch(code: String, title: String): DefaultTariffBatch {
    return DefaultTariffBatch(
        code = code,
        title = title,
        region = "",
        checksum = "",
        trailerId = "",
        budget = 0.0,
        startsAt = 0,
        maximum = 0.0,
        discount = 0.0,
        status = DefaultTariffBatchStatus.CREATED,
    )
}
