package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tariff variance is in its life cycle. */
enum class BatchedTariffVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched tariff variance, with what the planners need to act on it. */
data class BatchedTariffVariance(
    var code: String,
    var title: String,
    var weight: Double,
    var fuelLiters: Double,
    var odometer: Int,
    var source: String,
    var routeCode: String,
    var batchSize: Int,
    var reviewer: String,
    var status: BatchedTariffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffVarianceStatus): Boolean {
        when (status) {
            BatchedTariffVarianceStatus.NEW -> return next == BatchedTariffVarianceStatus.DONE
            BatchedTariffVarianceStatus.QUEUED -> {
                when (next) {
                    BatchedTariffVarianceStatus.RUNNING, BatchedTariffVarianceStatus.FAILED -> return true
                }
                return false
            }
            BatchedTariffVarianceStatus.RUNNING -> {
                when (next) {
                    BatchedTariffVarianceStatus.DONE -> return true
                }
                return false
            }
            BatchedTariffVarianceStatus.DONE -> {
                when (next) {
                    BatchedTariffVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTariffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched tariff variance"} $code"

    fun weightedTotal(): Double = round((fuelLiters - weight) * 100) / 100

    fun snapshot(): BatchedTariffVariance {
        return BatchedTariffVariance(
            code = code,
            title = title,
            weight = weight,
            fuelLiters = fuelLiters,
            odometer = odometer,
            source = source,
            routeCode = routeCode,
            batchSize = batchSize,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched tariff variance in status new. */
fun newBatchedTariffVariance(code: String, title: String): BatchedTariffVariance {
    return BatchedTariffVariance(
        code = code,
        title = title,
        weight = 0.0,
        fuelLiters = 0.0,
        odometer = 0,
        source = "",
        routeCode = "",
        batchSize = 0,
        reviewer = "",
        status = BatchedTariffVarianceStatus.NEW,
    )
}
