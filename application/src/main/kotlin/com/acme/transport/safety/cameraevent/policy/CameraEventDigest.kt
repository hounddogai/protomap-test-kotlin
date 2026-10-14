package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event digest is in its life cycle. */
enum class CameraEventDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a camera event digest and its current status. */
data class CameraEventDigest(
    var code: String,
    var label: String,
    var summary: String,
    var limit: Int,
    var sequence: Int,
    var active: Boolean,
    var region: String,
    var routeCode: String,
    var approved: Boolean,
    var threshold: Double,
    var status: CameraEventDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventDigestStatus): Boolean {
        when (status) {
            CameraEventDigestStatus.IDLE -> {
                when (next) {
                    CameraEventDigestStatus.WARMING -> return true
                }
                return false
            }
            CameraEventDigestStatus.WARMING -> return next == CameraEventDigestStatus.READY
            CameraEventDigestStatus.READY -> {
                when (next) {
                    CameraEventDigestStatus.DRAINING, CameraEventDigestStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventDigestStatus.DRAINING -> return next == CameraEventDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventDigestStatus) {
        if (!canMoveTo(next)) {
            error("camera event digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event digest"} $code"

    fun effectiveTotal(): Double = round((limit.toDouble() - threshold) * 100) / 100

    fun snapshot(): CameraEventDigest {
        return CameraEventDigest(
            code = code,
            label = label,
            summary = summary,
            limit = limit,
            sequence = sequence,
            active = active,
            region = region,
            routeCode = routeCode,
            approved = approved,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a camera event digest in status idle. */
fun newCameraEventDigest(code: String, label: String): CameraEventDigest {
    return CameraEventDigest(
        code = code,
        label = label,
        summary = "",
        limit = 0,
        sequence = 0,
        active = false,
        region = "",
        routeCode = "",
        approved = false,
        threshold = 0.0,
        status = CameraEventDigestStatus.IDLE,
    )
}
