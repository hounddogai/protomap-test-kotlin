package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan batch is in its life cycle. */
enum class LanePlanBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lane plan batch. Its code is unique within a tenant. */
data class LanePlanBatch(
    var code: String,
    var label: String,
    var stopCount: Int,
    var checksum: String,
    var revision: Int,
    var status: LanePlanBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanBatchStatus): Boolean {
        when (status) {
            LanePlanBatchStatus.NEW -> return next == LanePlanBatchStatus.FAILED
            LanePlanBatchStatus.QUEUED -> {
                when (next) {
                    LanePlanBatchStatus.RUNNING, LanePlanBatchStatus.DONE -> return true
                }
                return false
            }
            LanePlanBatchStatus.RUNNING -> {
                when (next) {
                    LanePlanBatchStatus.DONE, LanePlanBatchStatus.FAILED -> return true
                }
                return false
            }
            LanePlanBatchStatus.DONE -> return next == LanePlanBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LanePlanBatchStatus) {
        if (!canMoveTo(next)) {
            error("lane plan batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round(revision.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): LanePlanBatch {
        return LanePlanBatch(
            code = code,
            label = label,
            stopCount = stopCount,
            checksum = checksum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan batch in status new. */
fun newLanePlanBatch(code: String, label: String): LanePlanBatch {
    return LanePlanBatch(
        code = code,
        label = label,
        stopCount = 0,
        checksum = "",
        revision = 0,
        status = LanePlanBatchStatus.NEW,
    )
}
