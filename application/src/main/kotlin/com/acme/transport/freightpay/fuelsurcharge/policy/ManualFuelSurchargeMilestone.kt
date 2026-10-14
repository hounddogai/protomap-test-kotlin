package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel surcharge milestone is in its life cycle. */
enum class ManualFuelSurchargeMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One manual fuel surcharge milestone, with what the planners need to act on it. */
data class ManualFuelSurchargeMilestone(
    var code: String,
    var title: String,
    var country: String,
    var locale: String,
    var stopCount: Int,
    var limit: Int,
    var lane: String,
    var notes: String,
    var expiresAt: Int,
    var margin: Double,
    var etaMinutes: Int,
    var status: ManualFuelSurchargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelSurchargeMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelSurchargeMilestoneStatus): Boolean {
        when (status) {
            ManualFuelSurchargeMilestoneStatus.NEW -> return next == ManualFuelSurchargeMilestoneStatus.QUEUED
            ManualFuelSurchargeMilestoneStatus.QUEUED -> return next == ManualFuelSurchargeMilestoneStatus.DONE
            ManualFuelSurchargeMilestoneStatus.RUNNING -> {
                when (next) {
                    ManualFuelSurchargeMilestoneStatus.DONE, ManualFuelSurchargeMilestoneStatus.FAILED -> return true
                }
                return false
            }
            ManualFuelSurchargeMilestoneStatus.DONE -> {
                when (next) {
                    ManualFuelSurchargeMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFuelSurchargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel surcharge milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(etaMinutes.toDouble() * margin * 100) / 100

    fun snapshot(): ManualFuelSurchargeMilestone {
        return ManualFuelSurchargeMilestone(
            code = code,
            title = title,
            country = country,
            locale = locale,
            stopCount = stopCount,
            limit = limit,
            lane = lane,
            notes = notes,
            expiresAt = expiresAt,
            margin = margin,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual fuel surcharge milestone in status new. */
fun newManualFuelSurchargeMilestone(code: String, title: String): ManualFuelSurchargeMilestone {
    return ManualFuelSurchargeMilestone(
        code = code,
        title = title,
        country = "",
        locale = "",
        stopCount = 0,
        limit = 0,
        lane = "",
        notes = "",
        expiresAt = 0,
        margin = 0.0,
        etaMinutes = 0,
        status = ManualFuelSurchargeMilestoneStatus.NEW,
    )
}
