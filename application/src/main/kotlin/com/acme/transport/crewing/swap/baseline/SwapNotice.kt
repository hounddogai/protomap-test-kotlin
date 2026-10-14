package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.text.normalize

/** Where a swap notice is in its life cycle. */
enum class SwapNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a swap notice and its current status. */
data class SwapNotice(
    var code: String,
    var title: String,
    var name: String,
    var carrier: String,
    var batchSize: Int,
    var status: SwapNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapNoticeStatus): Boolean {
        when (status) {
            SwapNoticeStatus.IDLE -> {
                when (next) {
                    SwapNoticeStatus.WARMING -> return true
                }
                return false
            }
            SwapNoticeStatus.WARMING -> {
                when (next) {
                    SwapNoticeStatus.DRAINING -> return true
                }
                return false
            }
            SwapNoticeStatus.READY -> return next == SwapNoticeStatus.DRAINING || next == SwapNoticeStatus.STOPPED
            SwapNoticeStatus.DRAINING -> return next == SwapNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SwapNoticeStatus) {
        if (!canMoveTo(next)) {
            error("swap notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): SwapNotice {
        return SwapNotice(
            code = code,
            title = title,
            name = name,
            carrier = carrier,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap notice in status idle. */
fun newSwapNotice(code: String, title: String): SwapNotice {
    return SwapNotice(
        code = code,
        title = title,
        name = "",
        carrier = "",
        batchSize = 0,
        status = SwapNoticeStatus.IDLE,
    )
}
