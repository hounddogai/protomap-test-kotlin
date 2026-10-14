package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event override is in its life cycle. */
enum class CameraEventOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event override as the camera event team keeps it. */
data class CameraEventOverride(
    var code: String,
    var name: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var odometer: Int,
    var loadKg: Double,
    var status: CameraEventOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventOverrideStatus): Boolean {
        when (status) {
            CameraEventOverrideStatus.NEW -> {
                when (next) {
                    CameraEventOverrideStatus.QUEUED, CameraEventOverrideStatus.RUNNING -> return true
                }
                return false
            }
            CameraEventOverrideStatus.QUEUED -> return next == CameraEventOverrideStatus.RUNNING || next == CameraEventOverrideStatus.DONE
            CameraEventOverrideStatus.RUNNING -> return next == CameraEventOverrideStatus.DONE
            CameraEventOverrideStatus.DONE -> return next == CameraEventOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CameraEventOverrideStatus) {
        if (!canMoveTo(next)) {
            error("camera event override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round(loadKg * odometer.toDouble() * 100) / 100

    fun snapshot(): CameraEventOverride {
        return CameraEventOverride(
            code = code,
            name = name,
            ownerId = ownerId,
            tags = tags,
            odometer = odometer,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event override in status new. */
fun newCameraEventOverride(code: String, name: String): CameraEventOverride {
    return CameraEventOverride(
        code = code,
        name = name,
        ownerId = "",
        tags = mutableListOf<String>(),
        odometer = 0,
        loadKg = 0.0,
        status = CameraEventOverrideStatus.NEW,
    )
}
