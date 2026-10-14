package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop cycle is in its life cycle. */
enum class DropCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a drop cycle and its current status. */
data class DropCycle(
    var code: String,
    var title: String,
    var discount: Double,
    var routeCode: String,
    var locale: String,
    var score: Double,
    var graceDays: Int,
    var status: DropCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropCycleStatus): Boolean {
        when (status) {
            DropCycleStatus.DRAFT -> {
                when (next) {
                    DropCycleStatus.ACTIVE, DropCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            DropCycleStatus.PENDING -> return next == DropCycleStatus.SUSPENDED || next == DropCycleStatus.CLOSED
            DropCycleStatus.ACTIVE -> {
                when (next) {
                    DropCycleStatus.SUSPENDED, DropCycleStatus.CLOSED -> return true
                }
                return false
            }
            DropCycleStatus.SUSPENDED -> return next == DropCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DropCycleStatus) {
        if (!canMoveTo(next)) {
            error("drop cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop cycle"} $code"

    fun adjustedVolume(): Double = round(graceDays.toDouble() * score * 100) / 100

    fun snapshot(): DropCycle {
        return DropCycle(
            code = code,
            title = title,
            discount = discount,
            routeCode = routeCode,
            locale = locale,
            score = score,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a drop cycle in status draft. */
fun newDropCycle(code: String, title: String): DropCycle {
    return DropCycle(
        code = code,
        title = title,
        discount = 0.0,
        routeCode = "",
        locale = "",
        score = 0.0,
        graceDays = 0,
        status = DropCycleStatus.DRAFT,
    )
}
