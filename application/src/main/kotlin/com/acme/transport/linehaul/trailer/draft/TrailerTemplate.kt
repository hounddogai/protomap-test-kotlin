package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer template is in its life cycle. */
enum class TrailerTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a trailer template and its current status. */
data class TrailerTemplate(
    var code: String,
    var title: String,
    var ratio: Double,
    var visible: Boolean,
    var floorValue: Double,
    var channel: String,
    var status: TrailerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerTemplateStatus): Boolean {
        when (status) {
            TrailerTemplateStatus.DRAFT -> return next == TrailerTemplateStatus.PENDING || next == TrailerTemplateStatus.ACTIVE
            TrailerTemplateStatus.PENDING -> return next == TrailerTemplateStatus.ACTIVE || next == TrailerTemplateStatus.SUSPENDED
            TrailerTemplateStatus.ACTIVE -> {
                when (next) {
                    TrailerTemplateStatus.CLOSED -> return true
                }
                return false
            }
            TrailerTemplateStatus.SUSPENDED -> return next == TrailerTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("trailer template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((floorValue - ratio) * 100) / 100

    fun snapshot(): TrailerTemplate {
        return TrailerTemplate(
            code = code,
            title = title,
            ratio = ratio,
            visible = visible,
            floorValue = floorValue,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a trailer template in status draft. */
fun newTrailerTemplate(code: String, title: String): TrailerTemplate {
    return TrailerTemplate(
        code = code,
        title = title,
        ratio = 0.0,
        visible = false,
        floorValue = 0.0,
        channel = "",
        status = TrailerTemplateStatus.DRAFT,
    )
}
