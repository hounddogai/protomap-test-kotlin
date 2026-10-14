package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event baseline is in its life cycle. */
enum class CameraEventBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A camera event baseline. Its code is unique within a tenant. */
data class CameraEventBaseline(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var phone: String,
    var name: String,
    var limit: Int,
    var reviewer: String,
    var updatedAt: Int,
    var source: String,
    var revision: Int,
    var country: String,
    var status: CameraEventBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventBaselineStatus): Boolean {
        when (status) {
            CameraEventBaselineStatus.IDLE -> {
                when (next) {
                    CameraEventBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventBaselineStatus.WARMING -> {
                when (next) {
                    CameraEventBaselineStatus.DRAINING, CameraEventBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventBaselineStatus.READY -> {
                when (next) {
                    CameraEventBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventBaselineStatus.DRAINING -> return next == CameraEventBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventBaselineStatus) {
        if (!canMoveTo(next)) {
            error("camera event baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((limit.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): CameraEventBaseline {
        return CameraEventBaseline(
            code = code,
            label = label,
            pinned = pinned,
            phone = phone,
            name = name,
            limit = limit,
            reviewer = reviewer,
            updatedAt = updatedAt,
            source = source,
            revision = revision,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event baseline in status idle. */
fun newCameraEventBaseline(code: String, label: String): CameraEventBaseline {
    return CameraEventBaseline(
        code = code,
        label = label,
        pinned = false,
        phone = "",
        name = "",
        limit = 0,
        reviewer = "",
        updatedAt = 0,
        source = "",
        revision = 0,
        country = "",
        status = CameraEventBaselineStatus.IDLE,
    )
}
