package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default trailer record is in its life cycle. */
enum class DefaultTrailerRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default trailer record, with what the planners need to act on it. */
data class DefaultTrailerRecord(
    var code: String,
    var title: String,
    var summary: String,
    var visible: Boolean,
    var weight: Double,
    var endsAt: Int,
    var status: DefaultTrailerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerRecordStatus): Boolean {
        when (status) {
            DefaultTrailerRecordStatus.IDLE -> return next == DefaultTrailerRecordStatus.DRAINING
            DefaultTrailerRecordStatus.WARMING -> return next == DefaultTrailerRecordStatus.DRAINING || next == DefaultTrailerRecordStatus.STOPPED
            DefaultTrailerRecordStatus.READY -> return next == DefaultTrailerRecordStatus.STOPPED
            DefaultTrailerRecordStatus.DRAINING -> return next == DefaultTrailerRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultTrailerRecordStatus) {
        if (!canMoveTo(next)) {
            error("default trailer record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default trailer record"} $code"

    fun weightedTotal(): Double = round(endsAt.toDouble() * weight * 100) / 100

    fun snapshot(): DefaultTrailerRecord {
        return DefaultTrailerRecord(
            code = code,
            title = title,
            summary = summary,
            visible = visible,
            weight = weight,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default trailer record in status idle. */
fun newDefaultTrailerRecord(code: String, title: String): DefaultTrailerRecord {
    return DefaultTrailerRecord(
        code = code,
        title = title,
        summary = "",
        visible = false,
        weight = 0.0,
        endsAt = 0,
        status = DefaultTrailerRecordStatus.IDLE,
    )
}
