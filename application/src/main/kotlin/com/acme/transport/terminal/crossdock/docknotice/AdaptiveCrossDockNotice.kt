package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive cross dock notice records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a adaptive cross dock notice is in its life cycle. */
enum class AdaptiveCrossDockNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive cross dock notice as the cross dock team keeps it. */
data class AdaptiveCrossDockNotice(
    var code: String,
    var name: String,
    var batchSize: Int,
    var sequence: Int,
    var quantity: Int,
    var active: Boolean,
    var externalRef: String,
    var createdAt: Int,
    var ceiling: Double,
    var windowDays: Int,
    var channel: String,
    var status: AdaptiveCrossDockNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockNoticeStatus): Boolean {
        when (status) {
            AdaptiveCrossDockNoticeStatus.NEW -> return next == AdaptiveCrossDockNoticeStatus.DONE
            AdaptiveCrossDockNoticeStatus.QUEUED -> return next == AdaptiveCrossDockNoticeStatus.FAILED
            AdaptiveCrossDockNoticeStatus.RUNNING -> return next == AdaptiveCrossDockNoticeStatus.DONE
            AdaptiveCrossDockNoticeStatus.DONE -> return next == AdaptiveCrossDockNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockNoticeStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((sequence.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveCrossDockNotice {
        return AdaptiveCrossDockNotice(
            code = code,
            name = name,
            batchSize = batchSize,
            sequence = sequence,
            quantity = quantity,
            active = active,
            externalRef = externalRef,
            createdAt = createdAt,
            ceiling = ceiling,
            windowDays = windowDays,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive cross dock notice in status new. */
fun newAdaptiveCrossDockNotice(code: String, name: String): AdaptiveCrossDockNotice {
    return AdaptiveCrossDockNotice(
        code = code,
        name = name,
        batchSize = 0,
        sequence = 0,
        quantity = 0,
        active = false,
        externalRef = "",
        createdAt = 0,
        ceiling = 0.0,
        windowDays = 0,
        channel = "",
        status = AdaptiveCrossDockNoticeStatus.NEW,
    )
}
