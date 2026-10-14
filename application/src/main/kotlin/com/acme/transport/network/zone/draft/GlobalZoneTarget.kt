package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global zone target is in its life cycle. */
enum class GlobalZoneTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global zone target, with what the planners need to act on it. */
data class GlobalZoneTarget(
    var code: String,
    var title: String,
    var locale: String,
    var startsAt: Int,
    var rate: Double,
    var driverId: String,
    var externalRef: String,
    var cost: Double,
    var etaMinutes: Int,
    var sequence: Int,
    var capacity: Int,
    var status: GlobalZoneTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneTargetStatus): Boolean {
        when (status) {
            GlobalZoneTargetStatus.CREATED -> {
                when (next) {
                    GlobalZoneTargetStatus.VALIDATED, GlobalZoneTargetStatus.RETIRED -> return true
                }
                return false
            }
            GlobalZoneTargetStatus.VALIDATED -> return next == GlobalZoneTargetStatus.PUBLISHED || next == GlobalZoneTargetStatus.RETIRED
            GlobalZoneTargetStatus.PUBLISHED -> return next == GlobalZoneTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalZoneTargetStatus) {
        if (!canMoveTo(next)) {
            error("global zone target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global zone target"} $code"

    fun projectedExposure(): Double = round((startsAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): GlobalZoneTarget {
        return GlobalZoneTarget(
            code = code,
            title = title,
            locale = locale,
            startsAt = startsAt,
            rate = rate,
            driverId = driverId,
            externalRef = externalRef,
            cost = cost,
            etaMinutes = etaMinutes,
            sequence = sequence,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global zone target in status created. */
fun newGlobalZoneTarget(code: String, title: String): GlobalZoneTarget {
    return GlobalZoneTarget(
        code = code,
        title = title,
        locale = "",
        startsAt = 0,
        rate = 0.0,
        driverId = "",
        externalRef = "",
        cost = 0.0,
        etaMinutes = 0,
        sequence = 0,
        capacity = 0,
        status = GlobalZoneTargetStatus.CREATED,
    )
}
