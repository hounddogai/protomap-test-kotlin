package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual camera event digest is in its life cycle. */
enum class ManualCameraEventDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual camera event digest as the camera event team keeps it. */
data class ManualCameraEventDigest(
    var code: String,
    var label: String,
    var category: String,
    var retries: Int,
    var active: Boolean,
    var priority: Int,
    var score: Double,
    var segment: String,
    var version: Int,
    var routeCode: String,
    var status: ManualCameraEventDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventDigestStatus): Boolean {
        when (status) {
            ManualCameraEventDigestStatus.NEW -> return next == ManualCameraEventDigestStatus.DONE || next == ManualCameraEventDigestStatus.FAILED
            ManualCameraEventDigestStatus.QUEUED -> {
                when (next) {
                    ManualCameraEventDigestStatus.RUNNING, ManualCameraEventDigestStatus.DONE -> return true
                }
                return false
            }
            ManualCameraEventDigestStatus.RUNNING -> {
                when (next) {
                    ManualCameraEventDigestStatus.DONE, ManualCameraEventDigestStatus.FAILED -> return true
                }
                return false
            }
            ManualCameraEventDigestStatus.DONE -> return next == ManualCameraEventDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualCameraEventDigestStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"manual camera event digest"} $code"

    fun netVolume(): Double = round((retries.toDouble() - score) * 100) / 100

    fun snapshot(): ManualCameraEventDigest {
        return ManualCameraEventDigest(
            code = code,
            label = label,
            category = category,
            retries = retries,
            active = active,
            priority = priority,
            score = score,
            segment = segment,
            version = version,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual camera event digest in status new. */
fun newManualCameraEventDigest(code: String, label: String): ManualCameraEventDigest {
    return ManualCameraEventDigest(
        code = code,
        label = label,
        category = "",
        retries = 0,
        active = false,
        priority = 0,
        score = 0.0,
        segment = "",
        version = 0,
        routeCode = "",
        status = ManualCameraEventDigestStatus.NEW,
    )
}
