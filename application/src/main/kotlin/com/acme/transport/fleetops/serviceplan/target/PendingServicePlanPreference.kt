package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service plan preference is in its life cycle. */
enum class PendingServicePlanPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending service plan preference. Its code is unique within a tenant. */
data class PendingServicePlanPreference(
    var code: String,
    var title: String,
    var ceiling: Double,
    var graceDays: Int,
    var archived: Boolean,
    var stopCount: Int,
    var routeCode: String,
    var checksum: String,
    var status: PendingServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanPreferenceStatus): Boolean {
        when (status) {
            PendingServicePlanPreferenceStatus.PROPOSED -> {
                when (next) {
                    PendingServicePlanPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingServicePlanPreferenceStatus.APPROVED -> return next == PendingServicePlanPreferenceStatus.REJECTED || next == PendingServicePlanPreferenceStatus.WITHDRAWN
            PendingServicePlanPreferenceStatus.REJECTED -> {
                when (next) {
                    PendingServicePlanPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending service plan preference"} $code"

    fun weightedVolume(): Double = round(stopCount.toDouble() * ceiling * 100) / 100

    fun snapshot(): PendingServicePlanPreference {
        return PendingServicePlanPreference(
            code = code,
            title = title,
            ceiling = ceiling,
            graceDays = graceDays,
            archived = archived,
            stopCount = stopCount,
            routeCode = routeCode,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending service plan preference in status proposed. */
fun newPendingServicePlanPreference(code: String, title: String): PendingServicePlanPreference {
    return PendingServicePlanPreference(
        code = code,
        title = title,
        ceiling = 0.0,
        graceDays = 0,
        archived = false,
        stopCount = 0,
        routeCode = "",
        checksum = "",
        status = PendingServicePlanPreferenceStatus.PROPOSED,
    )
}
