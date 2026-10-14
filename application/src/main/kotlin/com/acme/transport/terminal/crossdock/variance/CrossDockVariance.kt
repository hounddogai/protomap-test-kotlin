package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock variance records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock variance is in its life cycle. */
enum class CrossDockVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cross dock variance and its current status. */
data class CrossDockVariance(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var batchSize: Int,
    var odometer: Int,
    var graceDays: Int,
    var status: CrossDockVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockVarianceStatus): Boolean {
        when (status) {
            CrossDockVarianceStatus.DRAFT -> {
                when (next) {
                    CrossDockVarianceStatus.PENDING -> return true
                }
                return false
            }
            CrossDockVarianceStatus.PENDING -> {
                when (next) {
                    CrossDockVarianceStatus.SUSPENDED, CrossDockVarianceStatus.CLOSED -> return true
                }
                return false
            }
            CrossDockVarianceStatus.ACTIVE -> return next == CrossDockVarianceStatus.SUSPENDED || next == CrossDockVarianceStatus.CLOSED
            CrossDockVarianceStatus.SUSPENDED -> return next == CrossDockVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CrossDockVarianceStatus) {
        if (!canMoveTo(next)) {
            error("cross dock variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((batchSize.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): CrossDockVariance {
        return CrossDockVariance(
            code = code,
            name = name,
            tags = tags,
            shiftCode = shiftCode,
            batchSize = batchSize,
            odometer = odometer,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cross dock variance in status draft. */
fun newCrossDockVariance(code: String, name: String): CrossDockVariance {
    return CrossDockVariance(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        shiftCode = "",
        batchSize = 0,
        odometer = 0,
        graceDays = 0,
        status = CrossDockVarianceStatus.DRAFT,
    )
}
