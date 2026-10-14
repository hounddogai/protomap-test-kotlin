package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary roster notice is in its life cycle. */
enum class PrimaryRosterNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary roster notice and its current status. */
data class PrimaryRosterNotice(
    var code: String,
    var label: String,
    var ratio: Double,
    var createdAt: Int,
    var score: Double,
    var threshold: Double,
    var status: PrimaryRosterNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterNoticeStatus): Boolean {
        when (status) {
            PrimaryRosterNoticeStatus.OPEN -> return next == PrimaryRosterNoticeStatus.REOPENED || next == PrimaryRosterNoticeStatus.ARCHIVED
            PrimaryRosterNoticeStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryRosterNoticeStatus.REOPENED, PrimaryRosterNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryRosterNoticeStatus.RESOLVED -> return next == PrimaryRosterNoticeStatus.REOPENED
            PrimaryRosterNoticeStatus.REOPENED -> return next == PrimaryRosterNoticeStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryRosterNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary roster notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((createdAt.toDouble() - ratio) * 100) / 100

    fun snapshot(): PrimaryRosterNotice {
        return PrimaryRosterNotice(
            code = code,
            label = label,
            ratio = ratio,
            createdAt = createdAt,
            score = score,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary roster notice in status open. */
fun newPrimaryRosterNotice(code: String, label: String): PrimaryRosterNotice {
    return PrimaryRosterNotice(
        code = code,
        label = label,
        ratio = 0.0,
        createdAt = 0,
        score = 0.0,
        threshold = 0.0,
        status = PrimaryRosterNoticeStatus.OPEN,
    )
}
