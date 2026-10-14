package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification notice is in its life cycle. */
enum class RegionalQualificationNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional qualification notice. Its code is unique within a tenant. */
data class RegionalQualificationNotice(
    var code: String,
    var title: String,
    var startsAt: Int,
    var trailerId: String,
    var driverId: String,
    var stopCount: Int,
    var quantity: Int,
    var amount: Double,
    var windowDays: Int,
    var reviewer: String,
    var status: RegionalQualificationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationNoticeStatus): Boolean {
        when (status) {
            RegionalQualificationNoticeStatus.PROPOSED -> {
                when (next) {
                    RegionalQualificationNoticeStatus.APPROVED, RegionalQualificationNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalQualificationNoticeStatus.APPROVED -> {
                when (next) {
                    RegionalQualificationNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalQualificationNoticeStatus.REJECTED -> return next == RegionalQualificationNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalQualificationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional qualification notice"} $code"

    fun effectiveTotal(): Double = round((stopCount.toDouble() - amount) * 100) / 100

    fun snapshot(): RegionalQualificationNotice {
        return RegionalQualificationNotice(
            code = code,
            title = title,
            startsAt = startsAt,
            trailerId = trailerId,
            driverId = driverId,
            stopCount = stopCount,
            quantity = quantity,
            amount = amount,
            windowDays = windowDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional qualification notice in status proposed. */
fun newRegionalQualificationNotice(code: String, title: String): RegionalQualificationNotice {
    return RegionalQualificationNotice(
        code = code,
        title = title,
        startsAt = 0,
        trailerId = "",
        driverId = "",
        stopCount = 0,
        quantity = 0,
        amount = 0.0,
        windowDays = 0,
        reviewer = "",
        status = RegionalQualificationNoticeStatus.PROPOSED,
    )
}
