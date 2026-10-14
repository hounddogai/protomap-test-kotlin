package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize

/** Where a lane plan notice is in its life cycle. */
enum class LanePlanNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lane plan notice and its current status. */
data class LanePlanNotice(
    var code: String,
    var name: String,
    var approved: Boolean,
    var phone: String,
    var etaMinutes: Int,
    var title: String,
    var driverId: String,
    var status: LanePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanNoticeStatus): Boolean {
        when (status) {
            LanePlanNoticeStatus.CREATED -> {
                when (next) {
                    LanePlanNoticeStatus.PUBLISHED, LanePlanNoticeStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanNoticeStatus.VALIDATED -> {
                when (next) {
                    LanePlanNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            LanePlanNoticeStatus.PUBLISHED -> return next == LanePlanNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LanePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("lane plan notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lane plan notice"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): LanePlanNotice {
        return LanePlanNotice(
            code = code,
            name = name,
            approved = approved,
            phone = phone,
            etaMinutes = etaMinutes,
            title = title,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan notice in status created. */
fun newLanePlanNotice(code: String, name: String): LanePlanNotice {
    return LanePlanNotice(
        code = code,
        name = name,
        approved = false,
        phone = "",
        etaMinutes = 0,
        title = "",
        driverId = "",
        status = LanePlanNoticeStatus.CREATED,
    )
}
