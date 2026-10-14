package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global accessorial score records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a global accessorial score is in its life cycle. */
enum class GlobalAccessorialScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global accessorial score and its current status. */
data class GlobalAccessorialScore(
    var code: String,
    var label: String,
    var stopCount: Int,
    var attempts: Int,
    var tags: MutableList<String>,
    var ownerId: String,
    var name: String,
    var routeCode: String,
    var phone: String,
    var unit: String,
    var status: GlobalAccessorialScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAccessorialScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAccessorialScoreStatus): Boolean {
        when (status) {
            GlobalAccessorialScoreStatus.PROPOSED -> return next == GlobalAccessorialScoreStatus.APPROVED || next == GlobalAccessorialScoreStatus.REJECTED
            GlobalAccessorialScoreStatus.APPROVED -> {
                when (next) {
                    GlobalAccessorialScoreStatus.REJECTED -> return true
                }
                return false
            }
            GlobalAccessorialScoreStatus.REJECTED -> return next == GlobalAccessorialScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalAccessorialScoreStatus) {
        if (!canMoveTo(next)) {
            error("global accessorial score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global accessorial score"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((attempts.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): GlobalAccessorialScore {
        return GlobalAccessorialScore(
            code = code,
            label = label,
            stopCount = stopCount,
            attempts = attempts,
            tags = tags,
            ownerId = ownerId,
            name = name,
            routeCode = routeCode,
            phone = phone,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global accessorial score in status proposed. */
fun newGlobalAccessorialScore(code: String, label: String): GlobalAccessorialScore {
    return GlobalAccessorialScore(
        code = code,
        label = label,
        stopCount = 0,
        attempts = 0,
        tags = mutableListOf<String>(),
        ownerId = "",
        name = "",
        routeCode = "",
        phone = "",
        unit = "",
        status = GlobalAccessorialScoreStatus.PROPOSED,
    )
}
