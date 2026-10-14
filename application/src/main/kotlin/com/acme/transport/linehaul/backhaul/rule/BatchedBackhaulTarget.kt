package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched backhaul target is in its life cycle. */
enum class BatchedBackhaulTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched backhaul target and its current status. */
data class BatchedBackhaulTarget(
    var code: String,
    var label: String,
    var endsAt: Int,
    var pinned: Boolean,
    var summary: String,
    var lane: String,
    var score: Double,
    var weight: Double,
    var status: BatchedBackhaulTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBackhaulTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBackhaulTargetStatus): Boolean {
        when (status) {
            BatchedBackhaulTargetStatus.NEW -> return next == BatchedBackhaulTargetStatus.FAILED
            BatchedBackhaulTargetStatus.QUEUED -> {
                when (next) {
                    BatchedBackhaulTargetStatus.DONE, BatchedBackhaulTargetStatus.FAILED -> return true
                }
                return false
            }
            BatchedBackhaulTargetStatus.RUNNING -> {
                when (next) {
                    BatchedBackhaulTargetStatus.DONE, BatchedBackhaulTargetStatus.FAILED -> return true
                }
                return false
            }
            BatchedBackhaulTargetStatus.DONE -> return next == BatchedBackhaulTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedBackhaulTargetStatus) {
        if (!canMoveTo(next)) {
            error("batched backhaul target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((score - endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedBackhaulTarget {
        return BatchedBackhaulTarget(
            code = code,
            label = label,
            endsAt = endsAt,
            pinned = pinned,
            summary = summary,
            lane = lane,
            score = score,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched backhaul target in status new. */
fun newBatchedBackhaulTarget(code: String, label: String): BatchedBackhaulTarget {
    return BatchedBackhaulTarget(
        code = code,
        label = label,
        endsAt = 0,
        pinned = false,
        summary = "",
        lane = "",
        score = 0.0,
        weight = 0.0,
        status = BatchedBackhaulTargetStatus.NEW,
    )
}
