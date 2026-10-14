package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback camera event record records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a fallback camera event record is in its life cycle. */
enum class FallbackCameraEventRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback camera event record as the camera event team keeps it. */
data class FallbackCameraEventRecord(
    var code: String,
    var title: String,
    var dock: String,
    var source: String,
    var durationMinutes: Int,
    var weight: Double,
    var etaMinutes: Int,
    var carrier: String,
    var quantity: Int,
    var createdAt: Int,
    var status: FallbackCameraEventRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCameraEventRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCameraEventRecordStatus): Boolean {
        when (status) {
            FallbackCameraEventRecordStatus.NEW -> return next == FallbackCameraEventRecordStatus.RUNNING || next == FallbackCameraEventRecordStatus.FAILED
            FallbackCameraEventRecordStatus.QUEUED -> return next == FallbackCameraEventRecordStatus.RUNNING
            FallbackCameraEventRecordStatus.RUNNING -> {
                when (next) {
                    FallbackCameraEventRecordStatus.DONE, FallbackCameraEventRecordStatus.FAILED -> return true
                }
                return false
            }
            FallbackCameraEventRecordStatus.DONE -> {
                when (next) {
                    FallbackCameraEventRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCameraEventRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback camera event record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((etaMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): FallbackCameraEventRecord {
        return FallbackCameraEventRecord(
            code = code,
            title = title,
            dock = dock,
            source = source,
            durationMinutes = durationMinutes,
            weight = weight,
            etaMinutes = etaMinutes,
            carrier = carrier,
            quantity = quantity,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback camera event record in status new. */
fun newFallbackCameraEventRecord(code: String, title: String): FallbackCameraEventRecord {
    return FallbackCameraEventRecord(
        code = code,
        title = title,
        dock = "",
        source = "",
        durationMinutes = 0,
        weight = 0.0,
        etaMinutes = 0,
        carrier = "",
        quantity = 0,
        createdAt = 0,
        status = FallbackCameraEventRecordStatus.NEW,
    )
}
