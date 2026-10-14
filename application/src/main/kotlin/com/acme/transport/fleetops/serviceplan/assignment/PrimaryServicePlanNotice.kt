package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service plan notice is in its life cycle. */
enum class PrimaryServicePlanNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary service plan notice, with what the planners need to act on it. */
data class PrimaryServicePlanNotice(
    var code: String,
    var label: String,
    var floorValue: Double,
    var capacity: Int,
    var loadKg: Double,
    var title: String,
    var status: PrimaryServicePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanNoticeStatus): Boolean {
        when (status) {
            PrimaryServicePlanNoticeStatus.PROPOSED -> {
                when (next) {
                    PrimaryServicePlanNoticeStatus.APPROVED, PrimaryServicePlanNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryServicePlanNoticeStatus.APPROVED -> {
                when (next) {
                    PrimaryServicePlanNoticeStatus.REJECTED, PrimaryServicePlanNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryServicePlanNoticeStatus.REJECTED -> {
                when (next) {
                    PrimaryServicePlanNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary service plan notice"} $code"

    fun effectiveExposure(): Double = round((loadKg - capacity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryServicePlanNotice {
        return PrimaryServicePlanNotice(
            code = code,
            label = label,
            floorValue = floorValue,
            capacity = capacity,
            loadKg = loadKg,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary service plan notice in status proposed. */
fun newPrimaryServicePlanNotice(code: String, label: String): PrimaryServicePlanNotice {
    return PrimaryServicePlanNotice(
        code = code,
        label = label,
        floorValue = 0.0,
        capacity = 0,
        loadKg = 0.0,
        title = "",
        status = PrimaryServicePlanNoticeStatus.PROPOSED,
    )
}
