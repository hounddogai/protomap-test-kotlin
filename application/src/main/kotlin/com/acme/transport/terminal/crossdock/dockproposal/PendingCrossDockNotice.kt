package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cross dock notice is in its life cycle. */
enum class PendingCrossDockNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending cross dock notice as the cross dock team keeps it. */
data class PendingCrossDockNotice(
    var code: String,
    var label: String,
    var amount: Double,
    var threshold: Double,
    var stopCount: Int,
    var cost: Double,
    var createdAt: Int,
    var quantity: Int,
    var ceiling: Double,
    var pallets: Int,
    var batchSize: Int,
    var status: PendingCrossDockNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCrossDockNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCrossDockNoticeStatus): Boolean {
        when (status) {
            PendingCrossDockNoticeStatus.OPEN -> {
                when (next) {
                    PendingCrossDockNoticeStatus.REOPENED -> return true
                }
                return false
            }
            PendingCrossDockNoticeStatus.IN_REVIEW -> return next == PendingCrossDockNoticeStatus.RESOLVED
            PendingCrossDockNoticeStatus.RESOLVED -> {
                when (next) {
                    PendingCrossDockNoticeStatus.REOPENED -> return true
                }
                return false
            }
            PendingCrossDockNoticeStatus.REOPENED -> {
                when (next) {
                    PendingCrossDockNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCrossDockNoticeStatus) {
        if (!canMoveTo(next)) {
            error("pending cross dock notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((createdAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): PendingCrossDockNotice {
        return PendingCrossDockNotice(
            code = code,
            label = label,
            amount = amount,
            threshold = threshold,
            stopCount = stopCount,
            cost = cost,
            createdAt = createdAt,
            quantity = quantity,
            ceiling = ceiling,
            pallets = pallets,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending cross dock notice in status open. */
fun newPendingCrossDockNotice(code: String, label: String): PendingCrossDockNotice {
    return PendingCrossDockNotice(
        code = code,
        label = label,
        amount = 0.0,
        threshold = 0.0,
        stopCount = 0,
        cost = 0.0,
        createdAt = 0,
        quantity = 0,
        ceiling = 0.0,
        pallets = 0,
        batchSize = 0,
        status = PendingCrossDockNoticeStatus.OPEN,
    )
}
