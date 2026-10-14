package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tariff rollup is in its life cycle. */
enum class BatchedTariffRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched tariff rollup. Its code is unique within a tenant. */
data class BatchedTariffRollup(
    var code: String,
    var name: String,
    var region: String,
    var retries: Int,
    var phone: String,
    var vehicleId: String,
    var visible: Boolean,
    var cost: Double,
    var status: BatchedTariffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffRollupStatus): Boolean {
        when (status) {
            BatchedTariffRollupStatus.CREATED -> return next == BatchedTariffRollupStatus.VALIDATED || next == BatchedTariffRollupStatus.RETIRED
            BatchedTariffRollupStatus.VALIDATED -> {
                when (next) {
                    BatchedTariffRollupStatus.PUBLISHED, BatchedTariffRollupStatus.RETIRED -> return true
                }
                return false
            }
            BatchedTariffRollupStatus.PUBLISHED -> return next == BatchedTariffRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedTariffRollupStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(cost * retries.toDouble() * 100) / 100

    fun snapshot(): BatchedTariffRollup {
        return BatchedTariffRollup(
            code = code,
            name = name,
            region = region,
            retries = retries,
            phone = phone,
            vehicleId = vehicleId,
            visible = visible,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched tariff rollup in status created. */
fun newBatchedTariffRollup(code: String, name: String): BatchedTariffRollup {
    return BatchedTariffRollup(
        code = code,
        name = name,
        region = "",
        retries = 0,
        phone = "",
        vehicleId = "",
        visible = false,
        cost = 0.0,
        status = BatchedTariffRollupStatus.CREATED,
    )
}
