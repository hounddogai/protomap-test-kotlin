package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling camera event audit records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a rolling camera event audit is in its life cycle. */
enum class RollingCameraEventAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling camera event audit, with what the planners need to act on it. */
data class RollingCameraEventAudit(
    var code: String,
    var label: String,
    var retries: Int,
    var score: Double,
    var loadKg: Double,
    var ratio: Double,
    var depot: String,
    var status: RollingCameraEventAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCameraEventAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCameraEventAuditStatus): Boolean {
        when (status) {
            RollingCameraEventAuditStatus.IDLE -> {
                when (next) {
                    RollingCameraEventAuditStatus.WARMING, RollingCameraEventAuditStatus.DRAINING -> return true
                }
                return false
            }
            RollingCameraEventAuditStatus.WARMING -> return next == RollingCameraEventAuditStatus.STOPPED
            RollingCameraEventAuditStatus.READY -> return next == RollingCameraEventAuditStatus.STOPPED
            RollingCameraEventAuditStatus.DRAINING -> return next == RollingCameraEventAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingCameraEventAuditStatus) {
        if (!canMoveTo(next)) {
            error("rolling camera event audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((loadKg + ratio) * 100) / 100

    fun snapshot(): RollingCameraEventAudit {
        return RollingCameraEventAudit(
            code = code,
            label = label,
            retries = retries,
            score = score,
            loadKg = loadKg,
            ratio = ratio,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling camera event audit in status idle. */
fun newRollingCameraEventAudit(code: String, label: String): RollingCameraEventAudit {
    return RollingCameraEventAudit(
        code = code,
        label = label,
        retries = 0,
        score = 0.0,
        loadKg = 0.0,
        ratio = 0.0,
        depot = "",
        status = RollingCameraEventAuditStatus.IDLE,
    )
}
