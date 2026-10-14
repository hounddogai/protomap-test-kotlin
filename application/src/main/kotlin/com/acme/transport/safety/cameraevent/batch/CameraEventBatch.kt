package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event batch records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event batch is in its life cycle. */
enum class CameraEventBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One camera event batch, with what the planners need to act on it. */
data class CameraEventBatch(
    var code: String,
    var name: String,
    var channel: String,
    var etaMinutes: Int,
    var category: String,
    var routeCode: String,
    var updatedAt: Int,
    var score: Double,
    var status: CameraEventBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventBatchStatus): Boolean {
        when (status) {
            CameraEventBatchStatus.NEW -> return next == CameraEventBatchStatus.QUEUED || next == CameraEventBatchStatus.RUNNING
            CameraEventBatchStatus.QUEUED -> return next == CameraEventBatchStatus.DONE || next == CameraEventBatchStatus.FAILED
            CameraEventBatchStatus.RUNNING -> {
                when (next) {
                    CameraEventBatchStatus.DONE, CameraEventBatchStatus.FAILED -> return true
                }
                return false
            }
            CameraEventBatchStatus.DONE -> return next == CameraEventBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CameraEventBatchStatus) {
        if (!canMoveTo(next)) {
            error("camera event batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CameraEventBatch {
        return CameraEventBatch(
            code = code,
            name = name,
            channel = channel,
            etaMinutes = etaMinutes,
            category = category,
            routeCode = routeCode,
            updatedAt = updatedAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a camera event batch in status new. */
fun newCameraEventBatch(code: String, name: String): CameraEventBatch {
    return CameraEventBatch(
        code = code,
        name = name,
        channel = "",
        etaMinutes = 0,
        category = "",
        routeCode = "",
        updatedAt = 0,
        score = 0.0,
        status = CameraEventBatchStatus.NEW,
    )
}
