package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched capacity metric is in its life cycle. */
enum class BatchedCapacityMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched capacity metric. Its code is unique within a tenant. */
data class BatchedCapacityMetric(
    var code: String,
    var title: String,
    var visible: Boolean,
    var unit: String,
    var createdAt: Int,
    var stopCount: Int,
    var name: String,
    var archived: Boolean,
    var checksum: String,
    var status: BatchedCapacityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityMetricStatus): Boolean {
        when (status) {
            BatchedCapacityMetricStatus.NEW -> {
                when (next) {
                    BatchedCapacityMetricStatus.RUNNING -> return true
                }
                return false
            }
            BatchedCapacityMetricStatus.QUEUED -> return next == BatchedCapacityMetricStatus.FAILED
            BatchedCapacityMetricStatus.RUNNING -> return next == BatchedCapacityMetricStatus.FAILED
            BatchedCapacityMetricStatus.DONE -> return next == BatchedCapacityMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedCapacityMetricStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((createdAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): BatchedCapacityMetric {
        return BatchedCapacityMetric(
            code = code,
            title = title,
            visible = visible,
            unit = unit,
            createdAt = createdAt,
            stopCount = stopCount,
            name = name,
            archived = archived,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched capacity metric in status new. */
fun newBatchedCapacityMetric(code: String, title: String): BatchedCapacityMetric {
    return BatchedCapacityMetric(
        code = code,
        title = title,
        visible = false,
        unit = "",
        createdAt = 0,
        stopCount = 0,
        name = "",
        archived = false,
        checksum = "",
        status = BatchedCapacityMetricStatus.NEW,
    )
}
