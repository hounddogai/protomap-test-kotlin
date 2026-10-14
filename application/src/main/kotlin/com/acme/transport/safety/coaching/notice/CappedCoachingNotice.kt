package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped coaching notice is in its life cycle. */
enum class CappedCoachingNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped coaching notice. Its code is unique within a tenant. */
data class CappedCoachingNotice(
    var code: String,
    var name: String,
    var phone: String,
    var tags: MutableList<String>,
    var margin: Double,
    var currency: String,
    var stopCount: Int,
    var reviewer: String,
    var threshold: Double,
    var status: CappedCoachingNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCoachingNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCoachingNoticeStatus): Boolean {
        when (status) {
            CappedCoachingNoticeStatus.REQUESTED -> return next == CappedCoachingNoticeStatus.GRANTED || next == CappedCoachingNoticeStatus.EXPIRED
            CappedCoachingNoticeStatus.GRANTED -> {
                when (next) {
                    CappedCoachingNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCoachingNoticeStatus.REVOKED -> {
                when (next) {
                    CappedCoachingNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCoachingNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped coaching notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round(margin * stopCount.toDouble() * 100) / 100

    fun snapshot(): CappedCoachingNotice {
        return CappedCoachingNotice(
            code = code,
            name = name,
            phone = phone,
            tags = tags,
            margin = margin,
            currency = currency,
            stopCount = stopCount,
            reviewer = reviewer,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped coaching notice in status requested. */
fun newCappedCoachingNotice(code: String, name: String): CappedCoachingNotice {
    return CappedCoachingNotice(
        code = code,
        name = name,
        phone = "",
        tags = mutableListOf<String>(),
        margin = 0.0,
        currency = "",
        stopCount = 0,
        reviewer = "",
        threshold = 0.0,
        status = CappedCoachingNoticeStatus.REQUESTED,
    )
}
