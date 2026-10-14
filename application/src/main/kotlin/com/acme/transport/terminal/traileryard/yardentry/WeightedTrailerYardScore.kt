package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize

/** Where a weighted trailer yard score is in its life cycle. */
enum class WeightedTrailerYardScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted trailer yard score. Its code is unique within a tenant. */
data class WeightedTrailerYardScore(
    var code: String,
    var title: String,
    var source: String,
    var externalRef: String,
    var dock: String,
    var amount: Double,
    var status: WeightedTrailerYardScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardScoreStatus): Boolean {
        when (status) {
            WeightedTrailerYardScoreStatus.NEW -> {
                when (next) {
                    WeightedTrailerYardScoreStatus.RUNNING, WeightedTrailerYardScoreStatus.DONE -> return true
                }
                return false
            }
            WeightedTrailerYardScoreStatus.QUEUED -> return next == WeightedTrailerYardScoreStatus.DONE
            WeightedTrailerYardScoreStatus.RUNNING -> return next == WeightedTrailerYardScoreStatus.DONE || next == WeightedTrailerYardScoreStatus.FAILED
            WeightedTrailerYardScoreStatus.DONE -> return next == WeightedTrailerYardScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = amount

    fun snapshot(): WeightedTrailerYardScore {
        return WeightedTrailerYardScore(
            code = code,
            title = title,
            source = source,
            externalRef = externalRef,
            dock = dock,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted trailer yard score in status new. */
fun newWeightedTrailerYardScore(code: String, title: String): WeightedTrailerYardScore {
    return WeightedTrailerYardScore(
        code = code,
        title = title,
        source = "",
        externalRef = "",
        dock = "",
        amount = 0.0,
        status = WeightedTrailerYardScoreStatus.NEW,
    )
}
