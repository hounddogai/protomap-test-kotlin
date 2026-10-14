package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event record is in its life cycle. */
enum class CameraEventRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a camera event record and its current status. */
data class CameraEventRecord(
    var code: String,
    var name: String,
    var budget: Double,
    var carrier: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var reviewer: String,
    var minimum: Double,
    var checksum: String,
    var description: String,
    var revision: Int,
    var status: CameraEventRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventRecordStatus): Boolean {
        when (status) {
            CameraEventRecordStatus.PLANNED -> return next == CameraEventRecordStatus.CANCELLED
            CameraEventRecordStatus.SCHEDULED -> return next == CameraEventRecordStatus.IN_PROGRESS || next == CameraEventRecordStatus.COMPLETED
            CameraEventRecordStatus.IN_PROGRESS -> {
                when (next) {
                    CameraEventRecordStatus.CANCELLED -> return true
                }
                return false
            }
            CameraEventRecordStatus.COMPLETED -> {
                when (next) {
                    CameraEventRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventRecordStatus) {
        if (!canMoveTo(next)) {
            error("camera event record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(minimum * budget * 100) / 100

    fun snapshot(): CameraEventRecord {
        return CameraEventRecord(
            code = code,
            name = name,
            budget = budget,
            carrier = carrier,
            attributes = attributes,
            unit = unit,
            reviewer = reviewer,
            minimum = minimum,
            checksum = checksum,
            description = description,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a camera event record in status planned. */
fun newCameraEventRecord(code: String, name: String): CameraEventRecord {
    return CameraEventRecord(
        code = code,
        name = name,
        budget = 0.0,
        carrier = "",
        attributes = mutableMapOf<String, String>(),
        unit = "",
        reviewer = "",
        minimum = 0.0,
        checksum = "",
        description = "",
        revision = 0,
        status = CameraEventRecordStatus.PLANNED,
    )
}
