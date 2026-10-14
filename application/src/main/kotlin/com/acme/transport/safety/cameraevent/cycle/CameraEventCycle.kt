package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event cycle records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event cycle is in its life cycle. */
enum class CameraEventCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A camera event cycle. Its code is unique within a tenant. */
data class CameraEventCycle(
    var code: String,
    var label: String,
    var maximum: Double,
    var reviewer: String,
    var source: String,
    var sequence: Int,
    var ratio: Double,
    var stopCount: Int,
    var checksum: String,
    var status: CameraEventCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventCycleStatus): Boolean {
        when (status) {
            CameraEventCycleStatus.DRAFT -> {
                when (next) {
                    CameraEventCycleStatus.PENDING, CameraEventCycleStatus.ACTIVE -> return true
                }
                return false
            }
            CameraEventCycleStatus.PENDING -> return next == CameraEventCycleStatus.CLOSED
            CameraEventCycleStatus.ACTIVE -> {
                when (next) {
                    CameraEventCycleStatus.CLOSED -> return true
                }
                return false
            }
            CameraEventCycleStatus.SUSPENDED -> {
                when (next) {
                    CameraEventCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventCycleStatus) {
        if (!canMoveTo(next)) {
            error("camera event cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((stopCount.toDouble() - maximum) * 100) / 100

    fun snapshot(): CameraEventCycle {
        return CameraEventCycle(
            code = code,
            label = label,
            maximum = maximum,
            reviewer = reviewer,
            source = source,
            sequence = sequence,
            ratio = ratio,
            stopCount = stopCount,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event cycle in status draft. */
fun newCameraEventCycle(code: String, label: String): CameraEventCycle {
    return CameraEventCycle(
        code = code,
        label = label,
        maximum = 0.0,
        reviewer = "",
        source = "",
        sequence = 0,
        ratio = 0.0,
        stopCount = 0,
        checksum = "",
        status = CameraEventCycleStatus.DRAFT,
    )
}
