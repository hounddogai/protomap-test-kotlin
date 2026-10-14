package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service plan estimate is in its life cycle. */
enum class ManualServicePlanEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual service plan estimate. Its code is unique within a tenant. */
data class ManualServicePlanEstimate(
    var code: String,
    var title: String,
    var notes: String,
    var graceDays: Int,
    var pinned: Boolean,
    var description: String,
    var revision: Int,
    var contactEmail: String,
    var status: ManualServicePlanEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanEstimateStatus): Boolean {
        when (status) {
            ManualServicePlanEstimateStatus.IDLE -> return next == ManualServicePlanEstimateStatus.WARMING || next == ManualServicePlanEstimateStatus.STOPPED
            ManualServicePlanEstimateStatus.WARMING -> return next == ManualServicePlanEstimateStatus.STOPPED
            ManualServicePlanEstimateStatus.READY -> {
                when (next) {
                    ManualServicePlanEstimateStatus.DRAINING, ManualServicePlanEstimateStatus.STOPPED -> return true
                }
                return false
            }
            ManualServicePlanEstimateStatus.DRAINING -> {
                when (next) {
                    ManualServicePlanEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualServicePlanEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((graceDays.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): ManualServicePlanEstimate {
        return ManualServicePlanEstimate(
            code = code,
            title = title,
            notes = notes,
            graceDays = graceDays,
            pinned = pinned,
            description = description,
            revision = revision,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual service plan estimate in status idle. */
fun newManualServicePlanEstimate(code: String, title: String): ManualServicePlanEstimate {
    return ManualServicePlanEstimate(
        code = code,
        title = title,
        notes = "",
        graceDays = 0,
        pinned = false,
        description = "",
        revision = 0,
        contactEmail = "",
        status = ManualServicePlanEstimateStatus.IDLE,
    )
}
