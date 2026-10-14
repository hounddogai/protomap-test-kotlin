package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped trailer rollup records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a capped trailer rollup is in its life cycle. */
enum class CappedTrailerRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped trailer rollup. Its code is unique within a tenant. */
data class CappedTrailerRollup(
    var code: String,
    var name: String,
    var dock: String,
    var pinned: Boolean,
    var description: String,
    var updatedAt: Int,
    var source: String,
    var durationMinutes: Int,
    var rate: Double,
    var status: CappedTrailerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerRollupStatus): Boolean {
        when (status) {
            CappedTrailerRollupStatus.NEW -> return next == CappedTrailerRollupStatus.FAILED
            CappedTrailerRollupStatus.QUEUED -> {
                when (next) {
                    CappedTrailerRollupStatus.RUNNING -> return true
                }
                return false
            }
            CappedTrailerRollupStatus.RUNNING -> return next == CappedTrailerRollupStatus.DONE || next == CappedTrailerRollupStatus.FAILED
            CappedTrailerRollupStatus.DONE -> {
                when (next) {
                    CappedTrailerRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((rate - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedTrailerRollup {
        return CappedTrailerRollup(
            code = code,
            name = name,
            dock = dock,
            pinned = pinned,
            description = description,
            updatedAt = updatedAt,
            source = source,
            durationMinutes = durationMinutes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped trailer rollup in status new. */
fun newCappedTrailerRollup(code: String, name: String): CappedTrailerRollup {
    return CappedTrailerRollup(
        code = code,
        name = name,
        dock = "",
        pinned = false,
        description = "",
        updatedAt = 0,
        source = "",
        durationMinutes = 0,
        rate = 0.0,
        status = CappedTrailerRollupStatus.NEW,
    )
}
