package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global lane plan item is in its life cycle. */
enum class GlobalLanePlanItemStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global lane plan item. Its code is unique within a tenant. */
data class GlobalLanePlanItem(
    var code: String,
    var title: String,
    var rate: Double,
    var etaMinutes: Int,
    var maximum: Double,
    var reviewer: String,
    var trailerId: String,
    var version: Int,
    var name: String,
    var status: GlobalLanePlanItemStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLanePlanItemStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLanePlanItemStatus): Boolean {
        when (status) {
            GlobalLanePlanItemStatus.IDLE -> {
                when (next) {
                    GlobalLanePlanItemStatus.DRAINING -> return true
                }
                return false
            }
            GlobalLanePlanItemStatus.WARMING -> return next == GlobalLanePlanItemStatus.READY || next == GlobalLanePlanItemStatus.DRAINING
            GlobalLanePlanItemStatus.READY -> {
                when (next) {
                    GlobalLanePlanItemStatus.DRAINING, GlobalLanePlanItemStatus.STOPPED -> return true
                }
                return false
            }
            GlobalLanePlanItemStatus.DRAINING -> return next == GlobalLanePlanItemStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalLanePlanItemStatus) {
        if (!canMoveTo(next)) {
            error("global lane plan item $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((maximum + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): GlobalLanePlanItem {
        return GlobalLanePlanItem(
            code = code,
            title = title,
            rate = rate,
            etaMinutes = etaMinutes,
            maximum = maximum,
            reviewer = reviewer,
            trailerId = trailerId,
            version = version,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global lane plan item in status idle. */
fun newGlobalLanePlanItem(code: String, title: String): GlobalLanePlanItem {
    return GlobalLanePlanItem(
        code = code,
        title = title,
        rate = 0.0,
        etaMinutes = 0,
        maximum = 0.0,
        reviewer = "",
        trailerId = "",
        version = 0,
        name = "",
        status = GlobalLanePlanItemStatus.IDLE,
    )
}
