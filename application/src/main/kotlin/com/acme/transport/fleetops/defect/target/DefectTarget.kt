package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect target records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect target is in its life cycle. */
enum class DefectTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One defect target, with what the planners need to act on it. */
data class DefectTarget(
    var code: String,
    var title: String,
    var locale: String,
    var expiresAt: Int,
    var routeCode: String,
    var summary: String,
    var ceiling: Double,
    var region: String,
    var revision: Int,
    var phone: String,
    var status: DefectTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectTargetStatus): Boolean {
        when (status) {
            DefectTargetStatus.IDLE -> {
                when (next) {
                    DefectTargetStatus.STOPPED -> return true
                }
                return false
            }
            DefectTargetStatus.WARMING -> {
                when (next) {
                    DefectTargetStatus.READY -> return true
                }
                return false
            }
            DefectTargetStatus.READY -> return next == DefectTargetStatus.DRAINING || next == DefectTargetStatus.STOPPED
            DefectTargetStatus.DRAINING -> {
                when (next) {
                    DefectTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectTargetStatus) {
        if (!canMoveTo(next)) {
            error("defect target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((expiresAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): DefectTarget {
        return DefectTarget(
            code = code,
            title = title,
            locale = locale,
            expiresAt = expiresAt,
            routeCode = routeCode,
            summary = summary,
            ceiling = ceiling,
            region = region,
            revision = revision,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a defect target in status idle. */
fun newDefectTarget(code: String, title: String): DefectTarget {
    return DefectTarget(
        code = code,
        title = title,
        locale = "",
        expiresAt = 0,
        routeCode = "",
        summary = "",
        ceiling = 0.0,
        region = "",
        revision = 0,
        phone = "",
        status = DefectTargetStatus.IDLE,
    )
}
