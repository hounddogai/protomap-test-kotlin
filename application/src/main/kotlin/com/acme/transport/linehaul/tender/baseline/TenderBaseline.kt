package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender baseline records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender baseline is in its life cycle. */
enum class TenderBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tender baseline as the tender team keeps it. */
data class TenderBaseline(
    var code: String,
    var label: String,
    var margin: Double,
    var endsAt: Int,
    var dock: String,
    var routeCode: String,
    var score: Double,
    var status: TenderBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderBaselineStatus): Boolean {
        when (status) {
            TenderBaselineStatus.IDLE -> return next == TenderBaselineStatus.WARMING || next == TenderBaselineStatus.READY
            TenderBaselineStatus.WARMING -> return next == TenderBaselineStatus.DRAINING
            TenderBaselineStatus.READY -> {
                when (next) {
                    TenderBaselineStatus.DRAINING, TenderBaselineStatus.STOPPED -> return true
                }
                return false
            }
            TenderBaselineStatus.DRAINING -> return next == TenderBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TenderBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tender baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round((endsAt.toDouble() - margin) * 100) / 100

    fun snapshot(): TenderBaseline {
        return TenderBaseline(
            code = code,
            label = label,
            margin = margin,
            endsAt = endsAt,
            dock = dock,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender baseline in status idle. */
fun newTenderBaseline(code: String, label: String): TenderBaseline {
    return TenderBaseline(
        code = code,
        label = label,
        margin = 0.0,
        endsAt = 0,
        dock = "",
        routeCode = "",
        score = 0.0,
        status = TenderBaselineStatus.IDLE,
    )
}
