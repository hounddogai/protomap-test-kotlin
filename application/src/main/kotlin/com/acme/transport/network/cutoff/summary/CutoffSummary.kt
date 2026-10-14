package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff summary records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff summary is in its life cycle. */
enum class CutoffSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff summary as the cutoff team keeps it. */
data class CutoffSummary(
    var code: String,
    var label: String,
    var score: Double,
    var limit: Int,
    var source: String,
    var tags: MutableList<String>,
    var routeCode: String,
    var country: String,
    var status: CutoffSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffSummaryStatus): Boolean {
        when (status) {
            CutoffSummaryStatus.NEW -> {
                when (next) {
                    CutoffSummaryStatus.QUEUED -> return true
                }
                return false
            }
            CutoffSummaryStatus.QUEUED -> {
                when (next) {
                    CutoffSummaryStatus.RUNNING -> return true
                }
                return false
            }
            CutoffSummaryStatus.RUNNING -> {
                when (next) {
                    CutoffSummaryStatus.DONE, CutoffSummaryStatus.FAILED -> return true
                }
                return false
            }
            CutoffSummaryStatus.DONE -> {
                when (next) {
                    CutoffSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffSummaryStatus) {
        if (!canMoveTo(next)) {
            error("cutoff summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff summary"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(limit.toDouble() * score * 100) / 100

    fun snapshot(): CutoffSummary {
        return CutoffSummary(
            code = code,
            label = label,
            score = score,
            limit = limit,
            source = source,
            tags = tags,
            routeCode = routeCode,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff summary in status new. */
fun newCutoffSummary(code: String, label: String): CutoffSummary {
    return CutoffSummary(
        code = code,
        label = label,
        score = 0.0,
        limit = 0,
        source = "",
        tags = mutableListOf<String>(),
        routeCode = "",
        country = "",
        status = CutoffSummaryStatus.NEW,
    )
}
