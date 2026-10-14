package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service plan group is in its life cycle. */
enum class BatchedServicePlanGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched service plan group. Its code is unique within a tenant. */
data class BatchedServicePlanGroup(
    var code: String,
    var name: String,
    var source: String,
    var rate: Double,
    var pallets: Int,
    var status: BatchedServicePlanGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServicePlanGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServicePlanGroupStatus): Boolean {
        when (status) {
            BatchedServicePlanGroupStatus.NEW -> return next == BatchedServicePlanGroupStatus.QUEUED
            BatchedServicePlanGroupStatus.QUEUED -> return next == BatchedServicePlanGroupStatus.DONE || next == BatchedServicePlanGroupStatus.FAILED
            BatchedServicePlanGroupStatus.RUNNING -> {
                when (next) {
                    BatchedServicePlanGroupStatus.FAILED -> return true
                }
                return false
            }
            BatchedServicePlanGroupStatus.DONE -> {
                when (next) {
                    BatchedServicePlanGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServicePlanGroupStatus) {
        if (!canMoveTo(next)) {
            error("batched service plan group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched service plan group"} $code"

    fun netVolume(): Double = round(rate * pallets.toDouble() * 100) / 100

    fun snapshot(): BatchedServicePlanGroup {
        return BatchedServicePlanGroup(
            code = code,
            name = name,
            source = source,
            rate = rate,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched service plan group in status new. */
fun newBatchedServicePlanGroup(code: String, name: String): BatchedServicePlanGroup {
    return BatchedServicePlanGroup(
        code = code,
        name = name,
        source = "",
        rate = 0.0,
        pallets = 0,
        status = BatchedServicePlanGroupStatus.NEW,
    )
}
