package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap group records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap group is in its life cycle. */
enum class SwapGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a swap group and its current status. */
data class SwapGroup(
    var code: String,
    var title: String,
    var category: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var ceiling: Double,
    var externalRef: String,
    var channel: String,
    var status: SwapGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapGroupStatus): Boolean {
        when (status) {
            SwapGroupStatus.OPEN -> {
                when (next) {
                    SwapGroupStatus.IN_REVIEW, SwapGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            SwapGroupStatus.IN_REVIEW -> {
                when (next) {
                    SwapGroupStatus.RESOLVED -> return true
                }
                return false
            }
            SwapGroupStatus.RESOLVED -> {
                when (next) {
                    SwapGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            SwapGroupStatus.REOPENED -> {
                when (next) {
                    SwapGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapGroupStatus) {
        if (!canMoveTo(next)) {
            error("swap group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap group"} $code"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() + ceiling) * 100) / 100

    fun snapshot(): SwapGroup {
        return SwapGroup(
            code = code,
            title = title,
            category = category,
            visible = visible,
            etaMinutes = etaMinutes,
            ceiling = ceiling,
            externalRef = externalRef,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a swap group in status open. */
fun newSwapGroup(code: String, title: String): SwapGroup {
    return SwapGroup(
        code = code,
        title = title,
        category = "",
        visible = false,
        etaMinutes = 0,
        ceiling = 0.0,
        externalRef = "",
        channel = "",
        status = SwapGroupStatus.OPEN,
    )
}
