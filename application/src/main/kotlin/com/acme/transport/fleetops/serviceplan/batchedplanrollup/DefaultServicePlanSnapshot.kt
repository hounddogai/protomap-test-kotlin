package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service plan snapshot is in its life cycle. */
enum class DefaultServicePlanSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default service plan snapshot and its current status. */
data class DefaultServicePlanSnapshot(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var status: DefaultServicePlanSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServicePlanSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServicePlanSnapshotStatus): Boolean {
        when (status) {
            DefaultServicePlanSnapshotStatus.OPEN -> {
                when (next) {
                    DefaultServicePlanSnapshotStatus.IN_REVIEW -> return true
                }
                return false
            }
            DefaultServicePlanSnapshotStatus.IN_REVIEW -> {
                when (next) {
                    DefaultServicePlanSnapshotStatus.RESOLVED, DefaultServicePlanSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultServicePlanSnapshotStatus.RESOLVED -> {
                when (next) {
                    DefaultServicePlanSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultServicePlanSnapshotStatus.REOPENED -> {
                when (next) {
                    DefaultServicePlanSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServicePlanSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default service plan snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default service plan snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): DefaultServicePlanSnapshot {
        return DefaultServicePlanSnapshot(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            amount = amount,
            attributes = attributes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default service plan snapshot in status open. */
fun newDefaultServicePlanSnapshot(code: String, title: String): DefaultServicePlanSnapshot {
    return DefaultServicePlanSnapshot(
        code = code,
        title = title,
        etaMinutes = 0,
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        status = DefaultServicePlanSnapshotStatus.OPEN,
    )
}
