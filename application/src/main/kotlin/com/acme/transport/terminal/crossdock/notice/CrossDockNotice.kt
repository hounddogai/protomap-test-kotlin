package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock notice records of terminal's cross dock work, and the rules, stores, and calculations around
// them.

/** Where a cross dock notice is in its life cycle. */
enum class CrossDockNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cross dock notice. Its code is unique within a tenant. */
data class CrossDockNotice(
    var code: String,
    var name: String,
    var version: Int,
    var threshold: Double,
    var amount: Double,
    var driverId: String,
    var source: String,
    var visible: Boolean,
    var routeCode: String,
    var quantity: Int,
    var status: CrossDockNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockNoticeStatus): Boolean {
        when (status) {
            CrossDockNoticeStatus.OPEN -> return next == CrossDockNoticeStatus.IN_REVIEW
            CrossDockNoticeStatus.IN_REVIEW -> {
                when (next) {
                    CrossDockNoticeStatus.REOPENED -> return true
                }
                return false
            }
            CrossDockNoticeStatus.RESOLVED -> return next == CrossDockNoticeStatus.REOPENED || next == CrossDockNoticeStatus.ARCHIVED
            CrossDockNoticeStatus.REOPENED -> return next == CrossDockNoticeStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockNoticeStatus) {
        if (!canMoveTo(next)) {
            error("cross dock notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((amount - threshold) * 100) / 100

    fun snapshot(): CrossDockNotice {
        return CrossDockNotice(
            code = code,
            name = name,
            version = version,
            threshold = threshold,
            amount = amount,
            driverId = driverId,
            source = source,
            visible = visible,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cross dock notice in status open. */
fun newCrossDockNotice(code: String, name: String): CrossDockNotice {
    return CrossDockNotice(
        code = code,
        name = name,
        version = 0,
        threshold = 0.0,
        amount = 0.0,
        driverId = "",
        source = "",
        visible = false,
        routeCode = "",
        quantity = 0,
        status = CrossDockNoticeStatus.OPEN,
    )
}
