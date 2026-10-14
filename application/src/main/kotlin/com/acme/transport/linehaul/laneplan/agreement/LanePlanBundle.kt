package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan bundle is in its life cycle. */
enum class LanePlanBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lane plan bundle as the lane plan team keeps it. */
data class LanePlanBundle(
    var code: String,
    var name: String,
    var reviewer: String,
    var visible: Boolean,
    var revision: Int,
    var windowDays: Int,
    var limit: Int,
    var attempts: Int,
    var status: LanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanBundleStatus): Boolean {
        when (status) {
            LanePlanBundleStatus.IDLE -> {
                when (next) {
                    LanePlanBundleStatus.READY -> return true
                }
                return false
            }
            LanePlanBundleStatus.WARMING -> {
                when (next) {
                    LanePlanBundleStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanBundleStatus.READY -> return next == LanePlanBundleStatus.DRAINING || next == LanePlanBundleStatus.STOPPED
            LanePlanBundleStatus.DRAINING -> {
                when (next) {
                    LanePlanBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("lane plan bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((revision.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): LanePlanBundle {
        return LanePlanBundle(
            code = code,
            name = name,
            reviewer = reviewer,
            visible = visible,
            revision = revision,
            windowDays = windowDays,
            limit = limit,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan bundle in status idle. */
fun newLanePlanBundle(code: String, name: String): LanePlanBundle {
    return LanePlanBundle(
        code = code,
        name = name,
        reviewer = "",
        visible = false,
        revision = 0,
        windowDays = 0,
        limit = 0,
        attempts = 0,
        status = LanePlanBundleStatus.IDLE,
    )
}
