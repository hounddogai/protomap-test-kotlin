package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss audit is in its life cycle. */
enum class NearMissAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A near miss audit. Its code is unique within a tenant. */
data class NearMissAudit(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var durationMinutes: Int,
    var country: String,
    var threshold: Double,
    var archived: Boolean,
    var status: NearMissAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissAuditStatus): Boolean {
        when (status) {
            NearMissAuditStatus.IDLE -> return next == NearMissAuditStatus.WARMING || next == NearMissAuditStatus.STOPPED
            NearMissAuditStatus.WARMING -> return next == NearMissAuditStatus.READY
            NearMissAuditStatus.READY -> {
                when (next) {
                    NearMissAuditStatus.STOPPED -> return true
                }
                return false
            }
            NearMissAuditStatus.DRAINING -> return next == NearMissAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NearMissAuditStatus) {
        if (!canMoveTo(next)) {
            error("near miss audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss audit"} $code"

    fun effectiveVolume(): Double = round((durationMinutes.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): NearMissAudit {
        return NearMissAudit(
            code = code,
            name = name,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            country = country,
            threshold = threshold,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a near miss audit in status idle. */
fun newNearMissAudit(code: String, name: String): NearMissAudit {
    return NearMissAudit(
        code = code,
        name = name,
        distanceKm = 0.0,
        durationMinutes = 0,
        country = "",
        threshold = 0.0,
        archived = false,
        status = NearMissAuditStatus.IDLE,
    )
}
