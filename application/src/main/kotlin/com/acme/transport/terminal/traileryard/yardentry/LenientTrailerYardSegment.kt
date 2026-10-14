package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient trailer yard segment is in its life cycle. */
enum class LenientTrailerYardSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient trailer yard segment as the trailer yard team keeps it. */
data class LenientTrailerYardSegment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var loadKg: Double,
    var driverId: String,
    var graceDays: Int,
    var dock: String,
    var status: LenientTrailerYardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTrailerYardSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTrailerYardSegmentStatus): Boolean {
        when (status) {
            LenientTrailerYardSegmentStatus.NEW -> return next == LenientTrailerYardSegmentStatus.DONE || next == LenientTrailerYardSegmentStatus.FAILED
            LenientTrailerYardSegmentStatus.QUEUED -> {
                when (next) {
                    LenientTrailerYardSegmentStatus.DONE, LenientTrailerYardSegmentStatus.FAILED -> return true
                }
                return false
            }
            LenientTrailerYardSegmentStatus.RUNNING -> return next == LenientTrailerYardSegmentStatus.DONE
            LenientTrailerYardSegmentStatus.DONE -> {
                when (next) {
                    LenientTrailerYardSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTrailerYardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient trailer yard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((loadKg - batchSize.toDouble()) * 100) / 100

    fun snapshot(): LenientTrailerYardSegment {
        return LenientTrailerYardSegment(
            code = code,
            label = label,
            batchSize = batchSize,
            loadKg = loadKg,
            driverId = driverId,
            graceDays = graceDays,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient trailer yard segment in status new. */
fun newLenientTrailerYardSegment(code: String, label: String): LenientTrailerYardSegment {
    return LenientTrailerYardSegment(
        code = code,
        label = label,
        batchSize = 0,
        loadKg = 0.0,
        driverId = "",
        graceDays = 0,
        dock = "",
        status = LenientTrailerYardSegmentStatus.NEW,
    )
}
