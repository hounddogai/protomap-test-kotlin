package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event entry is in its life cycle. */
enum class CameraEventEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One camera event entry, with what the planners need to act on it. */
data class CameraEventEntry(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var discount: Double,
    var version: Int,
    var weight: Double,
    var margin: Double,
    var status: CameraEventEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventEntryStatus): Boolean {
        when (status) {
            CameraEventEntryStatus.CREATED -> {
                when (next) {
                    CameraEventEntryStatus.VALIDATED -> return true
                }
                return false
            }
            CameraEventEntryStatus.VALIDATED -> return next == CameraEventEntryStatus.PUBLISHED
            CameraEventEntryStatus.PUBLISHED -> {
                when (next) {
                    CameraEventEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventEntryStatus) {
        if (!canMoveTo(next)) {
            error("camera event entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(weight * version.toDouble() * 100) / 100

    fun snapshot(): CameraEventEntry {
        return CameraEventEntry(
            code = code,
            name = name,
            updatedAt = updatedAt,
            discount = discount,
            version = version,
            weight = weight,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event entry in status created. */
fun newCameraEventEntry(code: String, name: String): CameraEventEntry {
    return CameraEventEntry(
        code = code,
        name = name,
        updatedAt = 0,
        discount = 0.0,
        version = 0,
        weight = 0.0,
        margin = 0.0,
        status = CameraEventEntryStatus.CREATED,
    )
}
