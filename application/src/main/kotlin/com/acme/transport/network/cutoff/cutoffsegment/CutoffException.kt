package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff exception is in its life cycle. */
enum class CutoffExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff exception as the cutoff team keeps it. */
data class CutoffException(
    var code: String,
    var title: String,
    var ratio: Double,
    var region: String,
    var loadKg: Double,
    var summary: String,
    var channel: String,
    var minimum: Double,
    var priority: Int,
    var windowDays: Int,
    var status: CutoffExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffExceptionStatus): Boolean {
        when (status) {
            CutoffExceptionStatus.NEW -> return next == CutoffExceptionStatus.DONE
            CutoffExceptionStatus.QUEUED -> return next == CutoffExceptionStatus.FAILED
            CutoffExceptionStatus.RUNNING -> return next == CutoffExceptionStatus.DONE
            CutoffExceptionStatus.DONE -> return next == CutoffExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffExceptionStatus) {
        if (!canMoveTo(next)) {
            error("cutoff exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round((windowDays.toDouble() - minimum) * 100) / 100

    fun snapshot(): CutoffException {
        return CutoffException(
            code = code,
            title = title,
            ratio = ratio,
            region = region,
            loadKg = loadKg,
            summary = summary,
            channel = channel,
            minimum = minimum,
            priority = priority,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a cutoff exception in status new. */
fun newCutoffException(code: String, title: String): CutoffException {
    return CutoffException(
        code = code,
        title = title,
        ratio = 0.0,
        region = "",
        loadKg = 0.0,
        summary = "",
        channel = "",
        minimum = 0.0,
        priority = 0,
        windowDays = 0,
        status = CutoffExceptionStatus.NEW,
    )
}
