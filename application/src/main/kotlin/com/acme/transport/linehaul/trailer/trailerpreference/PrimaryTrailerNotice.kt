package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.normalize

/** Where a primary trailer notice is in its life cycle. */
enum class PrimaryTrailerNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary trailer notice. Its code is unique within a tenant. */
data class PrimaryTrailerNotice(
    var code: String,
    var title: String,
    var currency: String,
    var tags: MutableList<String>,
    var floorValue: Double,
    var approved: Boolean,
    var status: PrimaryTrailerNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerNoticeStatus): Boolean {
        when (status) {
            PrimaryTrailerNoticeStatus.PLANNED -> return next == PrimaryTrailerNoticeStatus.SCHEDULED || next == PrimaryTrailerNoticeStatus.CANCELLED
            PrimaryTrailerNoticeStatus.SCHEDULED -> return next == PrimaryTrailerNoticeStatus.IN_PROGRESS || next == PrimaryTrailerNoticeStatus.CANCELLED
            PrimaryTrailerNoticeStatus.IN_PROGRESS -> return next == PrimaryTrailerNoticeStatus.CANCELLED
            PrimaryTrailerNoticeStatus.COMPLETED -> {
                when (next) {
                    PrimaryTrailerNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = floorValue

    fun snapshot(): PrimaryTrailerNotice {
        return PrimaryTrailerNotice(
            code = code,
            title = title,
            currency = currency,
            tags = tags,
            floorValue = floorValue,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary trailer notice in status planned. */
fun newPrimaryTrailerNotice(code: String, title: String): PrimaryTrailerNotice {
    return PrimaryTrailerNotice(
        code = code,
        title = title,
        currency = "",
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        approved = false,
        status = PrimaryTrailerNoticeStatus.PLANNED,
    )
}
