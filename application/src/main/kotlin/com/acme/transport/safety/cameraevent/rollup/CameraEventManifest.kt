package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize

/** Where a camera event manifest is in its life cycle. */
enum class CameraEventManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A camera event manifest as the camera event team keeps it. */
data class CameraEventManifest(
    var code: String,
    var name: String,
    var segment: String,
    var tags: MutableList<String>,
    var carrier: String,
    var region: String,
    var ownerId: String,
    var country: String,
    var shiftCode: String,
    var trailerId: String,
    var quantity: Int,
    var status: CameraEventManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventManifestStatus): Boolean {
        when (status) {
            CameraEventManifestStatus.IDLE -> return next == CameraEventManifestStatus.READY || next == CameraEventManifestStatus.STOPPED
            CameraEventManifestStatus.WARMING -> {
                when (next) {
                    CameraEventManifestStatus.DRAINING, CameraEventManifestStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventManifestStatus.READY -> {
                when (next) {
                    CameraEventManifestStatus.DRAINING, CameraEventManifestStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventManifestStatus.DRAINING -> {
                when (next) {
                    CameraEventManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventManifestStatus) {
        if (!canMoveTo(next)) {
            error("camera event manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CameraEventManifest {
        return CameraEventManifest(
            code = code,
            name = name,
            segment = segment,
            tags = tags,
            carrier = carrier,
            region = region,
            ownerId = ownerId,
            country = country,
            shiftCode = shiftCode,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a camera event manifest in status idle. */
fun newCameraEventManifest(code: String, name: String): CameraEventManifest {
    return CameraEventManifest(
        code = code,
        name = name,
        segment = "",
        tags = mutableListOf<String>(),
        carrier = "",
        region = "",
        ownerId = "",
        country = "",
        shiftCode = "",
        trailerId = "",
        quantity = 0,
        status = CameraEventManifestStatus.IDLE,
    )
}
