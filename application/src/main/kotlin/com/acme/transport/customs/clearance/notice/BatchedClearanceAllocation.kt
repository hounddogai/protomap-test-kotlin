package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched clearance allocation is in its life cycle. */
enum class BatchedClearanceAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched clearance allocation and its current status. */
data class BatchedClearanceAllocation(
    var code: String,
    var title: String,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var createdAt: Int,
    var dock: String,
    var locale: String,
    var phone: String,
    var priority: Int,
    var status: BatchedClearanceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedClearanceAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedClearanceAllocationStatus): Boolean {
        when (status) {
            BatchedClearanceAllocationStatus.NEW -> return next == BatchedClearanceAllocationStatus.QUEUED
            BatchedClearanceAllocationStatus.QUEUED -> {
                when (next) {
                    BatchedClearanceAllocationStatus.FAILED -> return true
                }
                return false
            }
            BatchedClearanceAllocationStatus.RUNNING -> return next == BatchedClearanceAllocationStatus.DONE || next == BatchedClearanceAllocationStatus.FAILED
            BatchedClearanceAllocationStatus.DONE -> {
                when (next) {
                    BatchedClearanceAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedClearanceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("batched clearance allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched clearance allocation"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((createdAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedClearanceAllocation {
        return BatchedClearanceAllocation(
            code = code,
            title = title,
            batchSize = batchSize,
            attributes = attributes,
            createdAt = createdAt,
            dock = dock,
            locale = locale,
            phone = phone,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched clearance allocation in status new. */
fun newBatchedClearanceAllocation(code: String, title: String): BatchedClearanceAllocation {
    return BatchedClearanceAllocation(
        code = code,
        title = title,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        createdAt = 0,
        dock = "",
        locale = "",
        phone = "",
        priority = 0,
        status = BatchedClearanceAllocationStatus.NEW,
    )
}
