package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled qualification notice is in its life cycle. */
enum class ScheduledQualificationNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scheduled qualification notice, with what the planners need to act on it. */
data class ScheduledQualificationNotice(
    var code: String,
    var title: String,
    var category: String,
    var score: Double,
    var threshold: Double,
    var expiresAt: Int,
    var etaMinutes: Int,
    var shiftCode: String,
    var endsAt: Int,
    var status: ScheduledQualificationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledQualificationNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledQualificationNoticeStatus): Boolean {
        when (status) {
            ScheduledQualificationNoticeStatus.PROPOSED -> {
                when (next) {
                    ScheduledQualificationNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledQualificationNoticeStatus.APPROVED -> {
                when (next) {
                    ScheduledQualificationNoticeStatus.REJECTED -> return true
                }
                return false
            }
            ScheduledQualificationNoticeStatus.REJECTED -> return next == ScheduledQualificationNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledQualificationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled qualification notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((score + threshold) * 100) / 100

    fun snapshot(): ScheduledQualificationNotice {
        return ScheduledQualificationNotice(
            code = code,
            title = title,
            category = category,
            score = score,
            threshold = threshold,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled qualification notice in status proposed. */
fun newScheduledQualificationNotice(code: String, title: String): ScheduledQualificationNotice {
    return ScheduledQualificationNotice(
        code = code,
        title = title,
        category = "",
        score = 0.0,
        threshold = 0.0,
        expiresAt = 0,
        etaMinutes = 0,
        shiftCode = "",
        endsAt = 0,
        status = ScheduledQualificationNoticeStatus.PROPOSED,
    )
}
