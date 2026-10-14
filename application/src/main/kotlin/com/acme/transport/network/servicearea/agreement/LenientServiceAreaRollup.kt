package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area rollup is in its life cycle. */
enum class LenientServiceAreaRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient service area rollup as the service area team keeps it. */
data class LenientServiceAreaRollup(
    var code: String,
    var title: String,
    var loadKg: Double,
    var stopCount: Int,
    var ratio: Double,
    var summary: String,
    var cost: Double,
    var windowDays: Int,
    var status: LenientServiceAreaRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaRollupStatus): Boolean {
        when (status) {
            LenientServiceAreaRollupStatus.NEW -> return next == LenientServiceAreaRollupStatus.QUEUED
            LenientServiceAreaRollupStatus.QUEUED -> return next == LenientServiceAreaRollupStatus.RUNNING || next == LenientServiceAreaRollupStatus.DONE
            LenientServiceAreaRollupStatus.RUNNING -> return next == LenientServiceAreaRollupStatus.DONE || next == LenientServiceAreaRollupStatus.FAILED
            LenientServiceAreaRollupStatus.DONE -> {
                when (next) {
                    LenientServiceAreaRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaRollupStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((loadKg - cost) * 100) / 100

    fun snapshot(): LenientServiceAreaRollup {
        return LenientServiceAreaRollup(
            code = code,
            title = title,
            loadKg = loadKg,
            stopCount = stopCount,
            ratio = ratio,
            summary = summary,
            cost = cost,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient service area rollup in status new. */
fun newLenientServiceAreaRollup(code: String, title: String): LenientServiceAreaRollup {
    return LenientServiceAreaRollup(
        code = code,
        title = title,
        loadKg = 0.0,
        stopCount = 0,
        ratio = 0.0,
        summary = "",
        cost = 0.0,
        windowDays = 0,
        status = LenientServiceAreaRollupStatus.NEW,
    )
}
